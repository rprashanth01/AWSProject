package com.cloud_prml;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsRequest;
import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.amazonaws.services.lightsail.model.StartInstanceRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.Base64;
import com.amazonaws.util.IOUtils;

public class EC2Commands {
    static AmazonEC2 amazonEC2Client;
    static String keyName = "FirstInstance";
    static String sgName = "default";
    static AmazonS3 s3client;
    static String ACCESS = "AKIAJ43BAZOUHMYAWH2Q";
    static String SECRET = "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";
    static String webId = "i-0e03946b80d65df76"; 
    
	public EC2Commands(){
		// TODO Auto-generated method stub
		BasicAWSCredentials cred = new BasicAWSCredentials(ACCESS,SECRET);
		AWSCredentialsProvider credentialsProvider = new  AWSStaticCredentialsProvider(cred);
         amazonEC2Client = AmazonEC2ClientBuilder.standard().withRegion("us-west-2").withCredentials(credentialsProvider).build();
         s3client = new AmazonS3Client(credentialsProvider);
	}
	
//	public static void main(String[] args) throws InterruptedException{
//		EC2Commands ec = new EC2Commands();
////		String id = ec.createInstance("8");
////		ec.checkStatus("8");
////		ec.terminateInstance(id);
//		//ec.startWebInstance();
//		final AmazonCloudWatchClient client = client(ACCESS, SECRET);
//        final GetMetricStatisticsRequest request = request(webId); 
//        final GetMetricStatisticsResult result = result(client, request);
//        toStdOut(result, webId);  
////		
//	}
	
	
	
     
	static void createBucket(){
		String bucketName = "";
		Bucket buck = s3client.createBucket(bucketName);
		System.out.println(buck.toString());
	}
	
	public void listInstances(){
		DescribeInstancesResult result = amazonEC2Client.describeInstances();
		List<Reservation> listReservations = result.getReservations();
		for(Reservation res : listReservations ){
			List<Instance> listInstances = res.getInstances();
			for(Instance i : listInstances){
				System.out.println(i.getInstanceId());
				StopInstancesRequest st = new StopInstancesRequest();
				st.withInstanceIds(i.getInstanceId());
				amazonEC2Client.stopInstances(st);
			}
		}
	}
	
	public void getCPUUsage(){
		
	}
	
	public void startWebInstance(){
		StartInstancesRequest st = new StartInstancesRequest();
		st.withInstanceIds(webId);
		System.out.println(amazonEC2Client.startInstances(st));
	}
	
	static void listBuckets(){
		List<Bucket> buckets = s3client.listBuckets();
	}
	
	static void putObject(){
		String bucketName = "pifftprashanth";
		String key = "1";
		String content = "test";
		s3client.putObject(bucketName, key, content);
	}
	
	public String getObject(String key){
		S3Object object = s3client.getObject("pifftcomputed", key);
		InputStream objectData = object.getObjectContent();
		try {
			String theString = IOUtils.toString(objectData);
			//System.out.println(theString);
			objectData.close();
			return theString;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public boolean isObjectPresent(String key){
		return s3client.doesObjectExist("pifftprashanth", key);
	}
	
	public String createInstance(String key) throws InterruptedException{
		String userData = "#!/bin/bash\n cd /home/ec2-user/ \n ./mine.sh "+ key +" \n";
		String formattedString = Base64.encodeAsString(userData.getBytes());
		RunInstancesRequest run = new RunInstancesRequest();
		run.withImageId("ami-8bac22eb").withInstanceType("t2.micro").
		    withMinCount(1).withMaxCount(1).withKeyName(keyName).
		    withSecurityGroups(sgName).withUserData(formattedString);
		
		
		RunInstancesResult result = amazonEC2Client.runInstances(run);
		System.out.println(result.toString());
		Instance e2 = result.getReservation().getInstances().get(0);
		System.out.println(e2.getState());
		Thread.sleep(10000);
		return e2.getInstanceId();
	}
	
	public void checkStatus(String key) throws InterruptedException{
		int count = 0;
		while( count < 50){
			try{
			    if(s3client.getObjectAsString("pifftcomputed", key) != null){
				return ;
			    } 
			} catch (Exception e){
				
			}
			Thread.sleep(2000);
			count++;
		}
		
	}
	
	static void stopInstance(){
		StopInstancesRequest stop = new StopInstancesRequest();
		stop.withInstanceIds("");//instance id
		StopInstancesResult result = amazonEC2Client.stopInstances(stop);
		System.out.println(result.toString());
	}
	
	public void terminateInstance( String id){
	    TerminateInstancesRequest stop = new TerminateInstancesRequest();
		stop.withInstanceIds(id);//instance id
		TerminateInstancesResult result = amazonEC2Client.terminateInstances(stop);
		System.out.println(result.toString());
	}
	
	private static AmazonCloudWatchClient client(final String awsAccessKey, final String awsSecretKey) {
        return new AmazonCloudWatchClient(new BasicAWSCredentials(awsAccessKey, awsSecretKey));
    }

    private static GetMetricStatisticsRequest request(final String instanceId) {
        final long twentyFourHrs = 1000 * 60 * 60 * 24;
        final int oneHour = 60 * 60;
        return new GetMetricStatisticsRequest()
            .withStartTime(new Date(new Date().getTime()- twentyFourHrs))
            .withNamespace("AWS/EC2")
            .withPeriod(oneHour)
            .withDimensions(new Dimension().withName("InstanceId").withValue(instanceId))
            .withMetricName("CPUUtilization")
            .withStatistics("Average", "Maximum")
            .withEndTime(new Date());
    }

    private static GetMetricStatisticsResult result(
            final AmazonCloudWatchClient client, final GetMetricStatisticsRequest request) {
         return client.getMetricStatistics(request);
    }

    private static void toStdOut(final GetMetricStatisticsResult result, final String instanceId) {
        System.out.println(result); // outputs empty result: {Label: CPUUtilization,Datapoints: []}
        for (final Datapoint dataPoint : result.getDatapoints()) {
            System.out.printf("%s instance's average CPU utilization : %s%n", instanceId, dataPoint.getAverage());      
            System.out.printf("%s instance's max CPU utilization : %s%n", instanceId, dataPoint.getMaximum());
        }
    }
}
