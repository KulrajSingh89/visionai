package com.oracle.cloud.sdk.samples;

import com.oracle.bmc.Region;
import com.oracle.bmc.aivision.AIServiceVisionClient;
import com.oracle.bmc.aivision.model.*;
import com.oracle.bmc.aivision.requests.AnalyzeImageRequest;
import com.oracle.bmc.aivision.responses.AnalyzeImageResponse;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.GetNamespaceRequest;
import com.oracle.bmc.objectstorage.requests.ListObjectsRequest;
import com.oracle.bmc.objectstorage.responses.GetNamespaceResponse;
import com.oracle.bmc.objectstorage.responses.ListObjectsResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
public class AIVisionController {

    @CrossOrigin(origins = "http://localhost:8000/", allowedHeaders = "Requestor-Type")
    @ResponseBody
    @RequestMapping(value = "/ai/vision/image", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public AnalyzeImageResult processImage(@RequestParam(value = "region", defaultValue = "US_ASHBURN_1", required = false) String _region, @RequestParam(value = "bucket", defaultValue = "your-bucket", required = false) String _bucket, @RequestParam(value = "object", required = true) String _object) throws Exception {

        AuthenticationDetailsProvider provider = OCIConfig.getAuthenticationDetailsProvider();
        final AIServiceVisionClient aiServiceVisionClient = new AIServiceVisionClient(provider);
        aiServiceVisionClient.setRegion(Region.valueOf(_region));
        List<ImageFeature> features = new ArrayList<>();
        AnalyzeImageResponse response;

        GetNamespaceResponse namespaceResponse = new ObjectStorageClient(provider).getNamespace(GetNamespaceRequest.builder().build());
        String namespace = namespaceResponse.getValue();

        features.add(ImageClassificationFeature.builder().build());
        features.add(ImageTextDetectionFeature.builder().build());
        features.add(ImageObjectDetectionFeature.builder().build());

        ObjectStorageImageDetails objectStorageDocumentDetails = ObjectStorageImageDetails.builder().bucketName(_bucket).namespaceName(namespace).objectName(_object).build();

        AnalyzeImageDetails analyzeDocumentDetails = AnalyzeImageDetails.builder().features(features).image(objectStorageDocumentDetails).build();

        AnalyzeImageRequest request = AnalyzeImageRequest.builder().analyzeImageDetails(analyzeDocumentDetails).build();

        response = aiServiceVisionClient.analyzeImage(request);
        return response.getAnalyzeImageResult();
    }

    @CrossOrigin(origins = "http://localhost:8000/", allowedHeaders = "Requestor-Type")
    @ResponseBody
    @RequestMapping(value = "/ai/vision/objectlist", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ObjectListResponse> getObjectListWithUrls(@RequestParam(value = "bucket", defaultValue = "your-bucket", required = false) String bucket,
                                                          @RequestParam(value = "region", defaultValue = "US_ASHBURN_1", required = false) String region) throws Exception {
        AuthenticationDetailsProvider provider = OCIConfig.getAuthenticationDetailsProvider();
        try (ObjectStorage objStoreClient = new ObjectStorageClient(provider)) {
            GetNamespaceResponse namespaceResponse = objStoreClient.getNamespace(GetNamespaceRequest.builder().build());
            String namespace = namespaceResponse.getValue();

            ListObjectsRequest lor = ListObjectsRequest.builder().namespaceName(namespace).bucketName(bucket).build();

            System.err.println("Listing objects from bucket " + bucket);
            AtomicInteger idGenerator = new AtomicInteger(1);
            ListObjectsResponse response = objStoreClient.listObjects(lor);
            List<ObjectListResponse> objectListResponses = response.getListObjects().getObjects().stream().map(obj -> new ObjectListResponse(idGenerator.getAndIncrement(), obj.getName(), obj.getName(), generatePreSignedUrl(region, namespace, bucket, obj.getName()))).collect(Collectors.toList());

            return objectListResponses;
        } catch (Throwable e) {
            System.err.println("Error fetching object list from bucket " + e.getMessage());
            throw new Exception("Failed to fetch objects", e);
        }
    }

    private String generatePreSignedUrl(String region, String namespace, String bucket, String objectName) {
        String baseUrl = String.format("https://objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/", region, namespace, bucket);
        return baseUrl + objectName;
    }

}

