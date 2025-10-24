package br.com.gustavo.catalog.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;

@Service
public class S3Service {

    private static Logger LOG = LoggerFactory.getLogger(S3Service.class);

    @Autowired
    private AmazonS3 s3client;

    @Value("${s3.bucket}")
    private String bucketName;

    public URL uploadFile(MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename(); // nome original do arquivo
            String extension = FilenameUtils.getExtension(originalName); // extensao do arquivo (jpg, png, etc)
            String fileName = Instant.now() + "." + extension; // nome do arquivo no s3

            // enviando isso tudo la pro s3
            InputStream is = file.getInputStream();
            String contentType = file.getContentType(); // jpg png
            return uploadFile(is, fileName, contentType);
        }
        catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private URL uploadFile(InputStream is, String fileName, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        LOG.info("upload start");
        s3client.putObject(bucketName, fileName, is, metadata);
        LOG.info("upload finish");
        return s3client.getUrl(bucketName, fileName);
    }
}
