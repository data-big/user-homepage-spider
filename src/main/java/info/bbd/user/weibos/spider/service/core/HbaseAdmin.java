
package info.bbd.user.weibos.spider.service.core;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.regionserver.BloomType;
import org.apache.hadoop.hbase.regionserver.ConstantSizeRegionSplitPolicy;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.bbd.user.weibos.spider.service.pool.HbaseConfig;
import info.bbd.user.weibos.spider.service.pool.PoolConfig;

public class HbaseAdmin {

	private static final Logger logger = LoggerFactory.getLogger(HbaseAdmin.class);

	private Connection conn;

	private static HbaseAdmin sbi;

	public static HbaseAdmin getInstence() {

		sbi = new HbaseAdmin();
		/* 连接池配置 */
		PoolConfig config = new PoolConfig();
		config.setMaxTotal(20);
		config.setMaxIdle(5);
		config.setMaxWaitMillis(1000);
		config.setTestOnBorrow(true);

		/* Hbase配置 */
		Configuration hbaseConfig = HbaseConfig.getHbaseConf();
		/* 初始化连接池 */
		HbasePool pool = new HbasePool(config, hbaseConfig);
		/* 从连接池中获取对象 */
		sbi.conn = pool.getConnection();
		logger.info("hbase连接已建立");
		return sbi;
	}

	public void createTable(String tableName, String[] columnFamilys, short regionCount, long regionMaxSize)
			throws IOException {

		/* 获取Admin对象 */
		try (Admin admin = conn.getAdmin();) {
			boolean booleansExisted = admin.tableExists(TableName.valueOf(tableName));
			boolean isExisted = admin.isTableAvailable(TableName.valueOf(tableName));
			if (!booleansExisted || !isExisted) {
				// 创建数据表描述器
				HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));
				// 设置并添加列族描述器
				for (String columnFamily : columnFamilys) {
					HColumnDescriptor columnDescriptor = new HColumnDescriptor(columnFamily);
					// 数据压缩算法
					// columnDescriptor.setCompressionType(Compression.Algorithm.SNAPPY);
					// 块大小
					columnDescriptor.setBlocksize(256 * 1024);
					// 布隆过滤器类型
					columnDescriptor.setBloomFilterType(BloomType.ROW);
					tableDescriptor.addFamily(columnDescriptor);
				}
				// 设置最大文件大小
				tableDescriptor.setMaxFileSize(regionMaxSize);
				// 根据最大常量值设置分区策略
				tableDescriptor.setValue(HTableDescriptor.SPLIT_POLICY, ConstantSizeRegionSplitPolicy.class.getName());

				// 分区数和范围
				regionCount = (short) Math.abs(regionCount);
				int regionRange = Short.MAX_VALUE / regionCount;
				// 分区key
				int counter = 0;
				byte[][] splitKeys = new byte[regionCount][];
				for (int i = 0; i < splitKeys.length; i++) {
					counter = counter + regionRange;
					String key = StringUtils.leftPad(Integer.toString(counter), 5, '0');
					splitKeys[i] = Bytes.toBytes(key);
					logger.info(" - Split: " + i + " '" + key + "'");
				}
				admin.createTable(tableDescriptor, splitKeys);
			}
		}
	}

	public void createTable(String tableName, String[] columnFamilys) {

		/* 获取Admin对象 */
		try (Admin admin = conn.getAdmin();) {
			boolean booleansExisted = admin.tableExists(TableName.valueOf(tableName));
			boolean isExisted = admin.isTableAvailable(TableName.valueOf(tableName));
			if (!booleansExisted || !isExisted) {
				// 创建数据表描述器
				HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));
				// 设置并添加列族描述器
				for (String columnFamily : columnFamilys) {
					HColumnDescriptor columnDescriptor = new HColumnDescriptor(columnFamily);
					tableDescriptor.addFamily(columnDescriptor);
				}

				admin.createTable(tableDescriptor);
			}
		} catch (Exception e) {

		}

	}

	/**
	 * 添加数据
	 * 
	 * @param connect
	 * @param rowKey
	 * @param tableName
	 * @param column1
	 * @param value1
	 * @param column2
	 * @param value2
	 * @throws IOException
	 */
	public void addData(String rowKey, String tableName, String[] column1, String[] value1) throws IOException {

		TableName tn = TableName.valueOf(tableName);// HTabel负责跟记录相关的操作如增删改查等//
		Table table = conn.getTable(tn); // 获取表
		Put put = new Put(Bytes.toBytes(rowKey));// 设置rowkey
		HColumnDescriptor[] columnFamilies = table.getTableDescriptor() // 获取所有的列族
				.getColumnFamilies();
		for (int i = 0; i < columnFamilies.length; i++) {
			String familyName = columnFamilies[i].getNameAsString(); // 获取列族名
			if (familyName.equals("user_info")) { // user_info列族put数据
				for (int j = 0; j < column1.length; j++) {
					put.addColumn(Bytes.toBytes(familyName), Bytes.toBytes(column1[j]), Bytes.toBytes(value1[j]));
				}
			}
			table.put(put);
		}
	}

	public void scanAll() throws IOException {
		int count = 0;
		int allCount = 0;
		TableName tn = TableName.valueOf("sina_user_info");
		Scan scan = new Scan();
		Table table = conn.getTable(tn);
		ResultScanner resultScanner = table.getScanner(scan);// 获得scan结果集
		for (Result rs : resultScanner) {
			List<Cell> cs = rs.listCells();// 将到的每一个结果，转成list的形式
			for (Cell cell : cs) {
				allCount++;
				String rowkey = Bytes.toString(CellUtil.cloneRow(cell));// 取到行键
				long timestamp = cell.getTimestamp();// 取时间戳
				String fname = Bytes.toString(CellUtil.cloneFamily(cell));// 取到列族名
				String qualifier = Bytes.toString(CellUtil.cloneQualifier(cell));// 取修饰名，即列名
				String value = Bytes.toString(CellUtil.cloneValue(cell)); // 取值
				if(qualifier.equals("label")) {
					if(value.length()>0 && value != null) {
						count++;
						logger.info("count:{}",count);
						break;
					}
				}
			}
		}
		logger.info("label count:{}", count);
		logger.info("all count:{}", allCount);
	}

	public long rowCount() {
		long rowCount = 0;
		try {
			TableName tn = TableName.valueOf("sina_user_info");
			Scan scan = new Scan();
			Table table = conn.getTable(tn);
			scan.setFilter(new FirstKeyOnlyFilter());
			ResultScanner resultScanner = table.getScanner(scan);
			for (Result result : resultScanner) {
				rowCount += result.size();
			}
		} catch (IOException e) {
			logger.info(e.getMessage(), e);
		}
		return rowCount;
	}

}
