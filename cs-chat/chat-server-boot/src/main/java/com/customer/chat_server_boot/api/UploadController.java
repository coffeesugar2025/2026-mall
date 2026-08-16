package com.customer.chat_server_boot.api;

import com.customer.chat_server_boot.common.R;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @PostMapping("/image")
    public R<String> uploadImage(@RequestParam MultipartFile file) throws Exception {
        String suffix = FilenameUtils.getExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "." + suffix;
        File dir = new File("./upload");
        if(!dir.exists()) dir.mkdirs();
        File dest = new File(dir,fileName);
        file.transferTo(dest);
        String url = "http://127.0.0.1:8080/upload/" + fileName;
        return R.success(url);
    }
}
