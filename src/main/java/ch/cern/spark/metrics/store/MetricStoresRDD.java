package ch.cern.spark.metrics.store;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import ch.cern.spark.metrics.MonitorIDMetricIDs;
import scala.Tuple2;

public class MetricStoresRDD extends JavaRDD<Tuple2<MonitorIDMetricIDs, MetricStore>> {

    private static final long serialVersionUID = 3741287858945087706L;
    
    private static transient FileSystem fs = null;
    
    public MetricStoresRDD(JavaRDD<Tuple2<MonitorIDMetricIDs, MetricStore>> rdd) {
        super(rdd.rdd(), rdd.classTag());
    }
    
    public void save(String storing_path) throws IllegalArgumentException, IOException {
    		save(storing_path, collect());
    }
    
    protected static void save(String storing_path, List<Tuple2<MonitorIDMetricIDs, MetricStore>> metricStores) throws IllegalArgumentException, IOException {
        setFileSystem();
        
        Path finalFile = getStoringFile(storing_path);
        Path tmpFile = finalFile.suffix(".tmp");
        
        fs.mkdirs(tmpFile.getParent());

        ObjectOutputStream oos = new ObjectOutputStream(fs.create(tmpFile, true));
        oos.writeObject(metricStores);
        oos.close();
        
        if(canBeRead(tmpFile)){
            fs.delete(finalFile, false);
            fs.rename(tmpFile, finalFile);
        }
            
    }
    
    private static boolean canBeRead(Path tmpFile) throws IOException {
        setFileSystem();
        
        try {
            ObjectInputStream is = new ObjectInputStream(fs.open(tmpFile));
            is.readObject();
            is.close();
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    protected static List<Tuple2<MonitorIDMetricIDs, MetricStore>> load(String storing_path) throws IOException, ClassNotFoundException {
        setFileSystem();
        
        Path finalFile = getStoringFile(storing_path);
        Path tmpFile = finalFile.suffix(".tmp");
        
        if(!fs.exists(finalFile) && fs.exists(tmpFile))
    			fs.rename(tmpFile, finalFile);
        
        if(!fs.exists(finalFile))
        		return new LinkedList<Tuple2<MonitorIDMetricIDs, MetricStore>>();
        
        ObjectInputStream is = new ObjectInputStream(fs.open(finalFile));
        
        List<Tuple2<MonitorIDMetricIDs, MetricStore>> stores =  (List<Tuple2<MonitorIDMetricIDs, MetricStore>>) is.readObject();
        
        return stores;
    }
    
    public static MetricStoresRDD load(String storing_path, JavaSparkContext context) throws IOException, ClassNotFoundException {
    		return new MetricStoresRDD(context.parallelize(load(storing_path)));
    }
    
    private static void setFileSystem() throws IOException {
        if(fs == null)
            fs = FileSystem.get(new Configuration());
    }
    
    private static Path getStoringFile(String storing_path){
        return new Path(storing_path + "/metricStores/latest");
    }

}
