package com.youlai.boot.generate;

import cn.hutool.core.util.StrUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.Controller;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SpringMVC Controller 接口扫描器
 * 扫描指定包，提取所有RestController/Controller接口并输出到文件
 */
public class SpringMvcApiScanner {

    // 扫描包路径
    private final String scanPackage;
    // 输出文件路径
    private final String outputFilePath;

    // 控制器注解
    private static final List<Class> CONTROLLER_ANNOTATIONS =
        Arrays.asList(
            RestController.class,
            Controller.class
        );

    // 请求映射注解
    private static final Map<Class<? extends Annotation>, String> MAPPING_ANNO_MAP;

    static {
        MAPPING_ANNO_MAP = new HashMap<>();
        MAPPING_ANNO_MAP.put(RequestMapping.class, "");
        MAPPING_ANNO_MAP.put(GetMapping.class, "GET");
        MAPPING_ANNO_MAP.put(PostMapping.class, "POST");
        MAPPING_ANNO_MAP.put(PutMapping.class, "PUT");
        MAPPING_ANNO_MAP.put(DeleteMapping.class, "DELETE");
        MAPPING_ANNO_MAP.put(PatchMapping.class, "PATCH");
    }

    public SpringMvcApiScanner(String scanPackage, String outputFilePath) {
        this.scanPackage = scanPackage;
        this.outputFilePath = outputFilePath;
        // 清空文件，写入头部
        //FileUtil.writeUtf8("# SpringMVC 接口清单\n# 扫描包：" + scanPackage + "\n# 生成时间：" + new Date() + "\n\n", new File(outputFilePath));
    }

    /**
     * 入口：执行扫描并输出文件
     */
    public void scanAndWrite() throws Exception {
        // 1. 扫描包下所有class
        Set<Class<?>> controllerClasses = scanControllerClass();
        if (controllerClasses.isEmpty()) {
            //FileUtil.appendUtf8("未扫描到任何 @Controller / @RestController\n", outputFilePath);
            return;
        }

        List<ApiInfo> allApiList = new ArrayList<>();
        for (Class<?> clazz : controllerClasses) {
            List<ApiInfo> apiList = parseController(clazz);
            allApiList.addAll(apiList);
        }

        // 2. 格式化输出文本
        StringBuilder content = new StringBuilder();
        for (ApiInfo api : allApiList) {
            content.append("=====================================\n");
            content.append("控制器：").append(api.getControllerClassName()).append(" 【").append(api.getControllerDesc()).append("】\n");
            content.append("接口名称：").append(api.getApiName()).append("\n");
            content.append("请求地址：").append(api.getUrl()).append("\n");
            content.append("请求方式：").append(api.getHttpMethod()).append("\n");
            content.append("接口描述：").append(api.getApiDesc()).append("\n");
            content.append("方法签名：").append(api.getMethodFullName()).append("\n\n");
        }

        // 3. 写入文件
        //FileUtil.appendUtf8(content.toString(), outputFilePath);
        System.out.println("接口清单生成完成，文件路径：" + new File(outputFilePath).getAbsolutePath());
    }

    /**
     * 包扫描，筛选所有Controller类
     */
    private Set<Class<?>> scanControllerClass() throws Exception {
        Set<Class<?>> classSet = new HashSet<>();
        String searchPath = "classpath*:" + scanPackage.replace(".", "/") + "/**/*.class";
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(searchPath);
        CachingMetadataReaderFactory readerFactory = new CachingMetadataReaderFactory();

        for (Resource res : resources) {
            MetadataReader reader = readerFactory.getMetadataReader(res);
            String className = reader.getClassMetadata().getClassName();
            Class<?> clazz = Class.forName(className);
            // 判断是否标记控制器注解
            boolean isController = CONTROLLER_ANNOTATIONS.stream()
                .anyMatch(clazz::isAnnotationPresent);
            if (isController) {
                classSet.add(clazz);
            }
        }
        return classSet;
    }

    /**
     * 解析单个Controller，提取所有接口方法
     */
    private List<ApiInfo> parseController(Class<?> clazz) {
        List<ApiInfo> result = new ArrayList<>();
        // 类上 @RequestMapping 前缀
        String classPrefix = "";
        RequestMapping classMapping = clazz.getAnnotation(RequestMapping.class);
        if (classMapping != null && classMapping.value().length > 0) {
            classPrefix = classMapping.value()[0];
        }

        // 遍历所有public方法
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            ApiInfo api = parseMethod(clazz, method, classPrefix);
            if (api != null) {
                result.add(api);
            }
        }
        return result;
    }

    /**
     * 解析单个接口方法
     */
    private ApiInfo parseMethod(Class<?> clazz, Method method, String classPrefix) {
        ApiInfo api = new ApiInfo();
        api.setControllerClassName(clazz.getSimpleName());
        api.setClassPrefix(classPrefix);
        api.setMethodFullName(clazz.getName() + "#" + method.getName());

        List<String> httpMethods = new ArrayList<>();
        String methodPath = "";
        boolean isApiMethod = false;

        // 遍历所有映射注解
        for (Map.Entry<Class<? extends Annotation>, String> entry : MAPPING_ANNO_MAP.entrySet()) {
            Class<? extends Annotation> annoCls = entry.getKey();
            Annotation anno = method.getAnnotation(annoCls);
            if (anno == null) {
                continue;
            }
            isApiMethod = true;
            String methodType = entry.getValue();

            // 反射获取注解 value 路径
            try {
                String[] paths = (String[]) anno.getClass().getMethod("value").invoke(anno);
                if (paths.length > 0) {
                    methodPath = paths[0];
                }
            } catch (Exception e) {
                // 无value属性忽略
            }

            // 处理 RequestMapping 多method
            if (anno instanceof RequestMapping) {
                RequestMapping rm = (RequestMapping) anno;
                RequestMethod[] methods = rm.method();
                if (methods.length == 0) {
                    httpMethods.add("ALL");
                } else {
                    httpMethods.addAll(Arrays.stream(methods)
                        .map(RequestMethod::name)
                        .collect(Collectors.toList()));
                }
            } else {
                httpMethods.add(methodType);
            }
        }

        // 非接口方法直接返回
        if (!isApiMethod) {
            return null;
        }

        // 拼接完整URL
        String fullUrl = joinPath(classPrefix, methodPath);
        api.setUrl(fullUrl);
        api.setHttpMethod(httpMethods);
        api.setApiName(method.getName());
        // 简易注释（可扩展读取JavaDoc注释）
        api.setApiDesc("");
        api.setControllerDesc("");
        return api;
    }

    /**
     * 拼接路径，处理斜杠
     */
    private String joinPath(String prefix, String path) {
        if (StrUtil.isBlank(prefix)) return path;
        if (StrUtil.isBlank(path)) return prefix;
        prefix = StrUtil.removeSuffix(prefix, "/");
        path = StrUtil.removePrefix(path, "/");
        return prefix + "/" + path;
    }
}
