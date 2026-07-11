package com.policy.core.function;

import cn.hutool.core.util.StrUtil;
import com.policy.core.annotation.Executor;
import com.policy.core.annotation.Param;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import com.policy.core.annotation.Function;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 * <p>
 * 对field的值进行加密。
 * <p>
 * string     加密字符串
 * cryptoType 算法字符串,可选：MD2、MD5、SHA1、SHA-256、SHA-384、SHA-512，忽略大小写，默认MD5
 *
 * @author
 * @date 2021/2/9
 * @since 1.0.0
 */
@Slf4j
@Function
public class CryptoFunction {

    /**
     * 对field的值进行加密
     *
     * @param string     加密字符串
     * @param cryptoType 加密类型，默认md5
     * @return 密文
     */
    @Executor
    public String executor(@Param(value = "string", required = false) String string,
                           @Param(value = "cryptoType", required = false) String cryptoType) {
        if (StrUtil.isBlank(string)) {
            return string;
        }
        // 忽略大小写
        if ("MD2".equalsIgnoreCase(cryptoType)) {
            return DigestUtils.md2Hex(string);
        } else if ("SHA-384".equalsIgnoreCase(cryptoType)) {
            return DigestUtils.sha384Hex(string);
        } else if ("SHA-512".equalsIgnoreCase(cryptoType)) {
            return DigestUtils.sha512Hex(string);
        } else if ("SHA1".equalsIgnoreCase(cryptoType)) {
            return DigestUtils.sha1Hex(string);
        } else if ("SHA-256".equalsIgnoreCase(cryptoType)) {
            return DigestUtils.sha256Hex(string);
        } else {
            // md5 ...
            return DigestUtils.md5Hex(string);
        }
    }

}
