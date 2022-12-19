package com.luisurdaneta.lambda;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.util.BinaryUtils;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;


public class ProfileImageUploadHandler implements RequestHandler<Payload, String>{
    
    @Override
    public String handleRequest(Payload payload, Context context) {

        LambdaLogger logger = context.getLogger();

        //Get bucket and key from trigger
        String bucket = "profile-pic-bucket-luisurdaneta-personal-project";
        String key = payload.getKey();

        byte[] originalImg = BinaryUtils.fromBase64(payload.getImage());

        //Create instance of s3 client
        final AmazonS3 s3 = AmazonS3ClientBuilder
                .standard()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(
                                "AKIA2JDUHBK5SDBVFGFB",
                                "P4+Us4bddNR7ohv0lQ/4VYkYoe47aPXqrlDqcbhi"
                        )
                    )
                )
                .build();

        //Resize original image and save to s3
        try {

            //Create thumbnail
            Image thumbnail = ImageIO
                    .read(new ByteArrayInputStream(originalImg))
                    .getScaledInstance(100, 100, BufferedImage.SCALE_SMOOTH);
            BufferedImage bufferedThumbnail = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

            bufferedThumbnail.createGraphics().drawImage(thumbnail, 0, 0, null);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedThumbnail,"png",baos);


            //Store thumbnail
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setUserMetadata(Collections.singletonMap("content-type", "png"));

            s3.putObject(
                    bucket,
                    key,
                    new ByteArrayInputStream(baos.toByteArray()),
                    objectMetadata
            );
            logger.log("Upload successful");
            return "Upload successful";

        }
        catch (IOException | AmazonServiceException e) {
            logger.log(e.getMessage());
            e.printStackTrace();
            return "Upload failed";
        }
    }
}
