package info.bbd.user.weibos.spider.service.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

public class PutCahe {

	public static Configuration configuration;
	public static Connection connection;
	public static HTable table;

	public void init(String tableName) throws IOException {
		configuration = HBaseConfiguration.create();
		connection = ConnectionFactory.createConnection(configuration);
		table = (HTable) connection.getTable(TableName.valueOf(tableName));
	}

	/**
	 * 使用客户端写缓冲区
	 * @throws IOException
	 */
	public void toHbaseFlushCommit() throws IOException {
		table.setAutoFlushTo(false); // 默认情况下缓冲区是禁用的，设为false来激活缓冲区
		Put put1 = new Put(Bytes.toBytes("row1"));
		put1.addColumn(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"), Bytes.toBytes("value1"));
		table.put(put1);

		Put put2 = new Put(Bytes.toBytes("row2"));
		put2.addColumn(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"), Bytes.toBytes("value2"));
		table.put(put2);

		Put put3 = new Put(Bytes.toBytes("row3"));
		put3.addColumn(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"), Bytes.toBytes("value3"));
		table.put(put3);

		Get get = new Get(Bytes.toBytes("row1"));
		Result result = table.get(get);
		System.out.println("Result:" + result);

		table.flushCommits();// 强制刷写缓冲区会导致产生一个RPC操作

		Result result2 = table.get(get);
		System.out.println("Result:" + result2);

	}

	/**
	 * 使用列表向hbase中插入数据
	 * @throws IOException
	 */
	public void listPut() throws IOException {
		List<Put> puts = new ArrayList<>();

		Put put1 = new Put(Bytes.toBytes("row1"));
		put1.addColumn(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"), Bytes.toBytes("value1"));
		puts.add(put1);

		Put put2 = new Put(Bytes.toBytes("row2"));
		put2.addColumn(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"), Bytes.toBytes("value2"));
		puts.add(put2);

		Put put3 = new Put(Bytes.toBytes("row3"));
		put3.addColumn(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"), Bytes.toBytes("value3"));
		puts.add(put3);

		table.put(puts);

	}

	/**
	 * 向hhbase中插入一个错误的列族
	 * @throws IOException
	 */
	public void errorPut() throws IOException {
		List<Put> puts = new ArrayList<>();

		Put put1 = new Put(Bytes.toBytes("row1"));
		put1.addColumn(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"), Bytes.toBytes("value1"));
		puts.add(put1);

		Put put2 = new Put(Bytes.toBytes("row2"));
		put2.addColumn(Bytes.toBytes("ERROR"), Bytes.toBytes("qual1"), Bytes.toBytes("value2"));
		puts.add(put2);

		Put put3 = new Put(Bytes.toBytes("row3"));
		put3.addColumn(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"), Bytes.toBytes("value3"));
		puts.add(put3);

		table.put(puts);
	}

	/**
	 * 测试插入一个空的Put实例
	 */
	public void putNull() {
		List<Put> puts = new ArrayList<>();

		Put put1 = new Put(Bytes.toBytes("row1"));
		put1.addColumn(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"), Bytes.toBytes("value1"));
		puts.add(put1);

		Put put2 = new Put(Bytes.toBytes("row2"));
		put2.addColumn(Bytes.toBytes("ERROR"), Bytes.toBytes("qual1"), Bytes.toBytes("value2"));
		puts.add(put2);

		Put put3 = new Put(Bytes.toBytes("row3"));
		put3.addColumn(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"), Bytes.toBytes("value3"));
		puts.add(put3);

		Put put4 = new Put(Bytes.toBytes("row4"));
		puts.add(put4);
		
		try {
			table.put(puts);
		} catch (IOException e) {
			System.err.println("ERROR-1:"+e);
			try {
				table.flushCommits();
			} catch (IOException e1) {
				System.err.println("ERROR-2:"+e);
			}
		}
	}
	
	/**
	 * 原子性操作：检查写
	 * @throws IOException 
	 */
	public void compareAndSet() throws IOException {
		Put put1 = new Put(Bytes.toBytes("row1"));
		put1.addColumn(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"), Bytes.toBytes("value1"));
		boolean response1 = table.checkAndPut(Bytes.toBytes("row1"), Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"), null, put1);
		System.out.println("Put applied:"+response1);
		
		boolean response2 = table.checkAndPut(Bytes.toBytes("row1"), Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"), null, put1);
		System.out.println("Put applied:"+response2);
		
		Put put2 = new Put(Bytes.toBytes("row1"));
		put2.addColumn(Bytes.toBytes("colfam1"), Bytes.toBytes("qual2"), Bytes.toBytes("value2"));
		boolean response3 = table.checkAndPut(Bytes.toBytes("row1"), Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"), Bytes.toBytes("value1"), put2);
		System.out.println("Put applied:"+response3);
		
		Put put3 = new Put(Bytes.toBytes("row2"));
		put3.addColumn(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"), Bytes.toBytes("value3"));
		boolean response4 = table.checkAndPut(Bytes.toBytes("row1"), Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"), Bytes.toBytes("value1"), put3);
		System.out.println("Put applied:"+response4);
	}

	public static void main(String[] args) {
		PutCahe putCahe = new PutCahe();
		try {
			putCahe.init("testtable");
			// putCahe.toHbaseFlushCommit();
			// putCahe.listPut();
			// putCahe.errorPut();
			// putCahe.putNull();
			putCahe.compareAndSet();
			connection.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
