package ch.cern.spark.metrics.store;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.fs.Path;
import org.junit.Test;

import ch.cern.spark.RDD;
import ch.cern.spark.metrics.MonitorIDMetricIDs;
import ch.cern.spark.metrics.notifications.NotificationStoresRDDTest;
import scala.Tuple2;

public class MetricStoresRDDTest {
	
	@Test
    public void saveAndLoad() throws ClassNotFoundException, IOException{
		Path storingPath = new Path("/tmp/" + NotificationStoresRDDTest.class.toString());
    	
    		List<Tuple2<MonitorIDMetricIDs, MetricStore>> expectedStores = new LinkedList<>();
    		
    		MonitorIDMetricIDs id = new MonitorIDMetricIDs("monId1", new HashMap<>());
		MetricStore store = new MetricStore();
		store.setPreAnalysisStore(new TestStore(2));
		store.setAnalysisStore(new TestStore(3));
		expectedStores.add(new Tuple2<MonitorIDMetricIDs, MetricStore>(id, store));
		
		id = new MonitorIDMetricIDs("monId2", new HashMap<>());
		store = new MetricStore();
		store.setPreAnalysisStore(new TestStore(4));
		store.setAnalysisStore(new TestStore(5));
		expectedStores.add(new Tuple2<MonitorIDMetricIDs, MetricStore>(id, store));
    		
		RDD.save(storingPath, expectedStores);
    		
		List<Tuple2<MonitorIDMetricIDs, MetricStore>> loadedStores = RDD.<Tuple2<MonitorIDMetricIDs, MetricStore>>load(storingPath);
		
		assertEquals(expectedStores.get(0)._1, loadedStores.get(0)._1);
		assertEquals(expectedStores.get(0)._2.getPreAnalysisStore(), loadedStores.get(0)._2.getPreAnalysisStore());
		assertEquals(expectedStores.get(0)._2.getAnalysisStore(), loadedStores.get(0)._2.getAnalysisStore());
		
		assertEquals(expectedStores.get(1)._1, loadedStores.get(1)._1);
		assertEquals(expectedStores.get(1)._2.getPreAnalysisStore(), loadedStores.get(1)._2.getPreAnalysisStore());
		assertEquals(expectedStores.get(1)._2.getAnalysisStore(), loadedStores.get(1)._2.getAnalysisStore());
		
		assertEquals(expectedStores, RDD.<Tuple2<MonitorIDMetricIDs, MetricStore>>load(storingPath));
    }

}
