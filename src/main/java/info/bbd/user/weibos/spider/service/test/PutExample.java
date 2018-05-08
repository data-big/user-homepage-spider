package info.bbd.user.weibos.spider.service.test;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

public class PutExample {

	private static Connection connection;
	private static Configuration configuration;
	
	public static void main(String[] args) throws IOException {
		configuration = HBaseConfiguration.create();
//		configuration.set("hbase.zookeeper.quorum", "192.168.33.128:2181");
//		configuration.set("hbase.rootdir", "hdfs://192.168.33.128:9000/hbase");
		connection = ConnectionFactory.createConnection(configuration);
		Table table = connection.getTable(TableName.valueOf("testtable"));
		Put put = new Put(Bytes.toBytes("row4"));
		put.addColumn(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"), Bytes.toBytes("val4"));
		put.addColumn(Bytes.toBytes("colfam1"), Bytes.toBytes("qual2"), Bytes.toBytes("val4"));
		table.put(put);
	}
}
