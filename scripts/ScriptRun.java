import com.cloud_prml.EC2Commands;
public class ScriptRun {

 public static void main(String[] args){
     int firstArg = Integer.parseInt(args[0]);
	 EC2Commands ec = new EC2Commands();
	 switch(firstArg){
	 	case 1:
	 	     ec.startWebInstance();
	 	     break;
	 	case 2:
	 	     ec.stopAllInstances();
	 	     break;
	 	case 3:
	 	    ec.getCPUUsage();
	 	     break;
	 	case 4:
	 	    ec.listBuckets();
	 	    break;

	 }
 }
}