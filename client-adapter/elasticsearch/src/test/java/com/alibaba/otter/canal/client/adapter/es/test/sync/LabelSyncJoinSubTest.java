package com.alibaba.otter.canal.client.adapter.es.test.sync;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.elasticsearch.action.get.GetResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.alibaba.otter.canal.client.adapter.es.ESAdapter;
import com.alibaba.otter.canal.client.adapter.es.config.ESSyncConfig;
import com.alibaba.otter.canal.client.adapter.support.DatasourceConfig;
import com.alibaba.otter.canal.client.adapter.support.Dml;
@Ignore
public class LabelSyncJoinSubTest {

    private ESAdapter esAdapter;

    @Before
    public void init() {
        // AdapterConfigs.put("es", "mytest_user_join_sub.yml");
        esAdapter = Common.init();
    }

    /**
     * 子查询从表插入
     */
    @Test
    public void test01() {
        DataSource ds = DatasourceConfig.DATA_SOURCES.get("defaultDS");
        Common.sqlExe(ds, "delete from label where id=1 or id=2");
        Common.sqlExe(ds, "insert into label (id,user_id,label) values (1,1,'a')");
        Common.sqlExe(ds, "insert into label (id,user_id,label) values (2,1,'b')");

        Dml dml = new Dml();
        dml.setDestination("example");
        dml.setTs(new Date().getTime());
        dml.setType("INSERT");
        dml.setDatabase("mytest");
        dml.setTable("label");
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> data = new LinkedHashMap<>();
        dataList.add(data);
        data.put("id", 2L);
        data.put("user_id", 1L);
        data.put("label", "b");
        dml.setData(dataList);

        String database = dml.getDatabase();
        String table = dml.getTable();
        Map<String, ESSyncConfig> esSyncConfigs = esAdapter.getDbTableEsSyncConfig().get(database + "-" + table);

        esAdapter.getEsSyncService().sync(esSyncConfigs.values(), dml);

        GetResponse response = esAdapter.getTransportClient().prepareGet("mytest_user", "_doc", "1").get();
        Assert.assertEquals("b;a", response.getSource().get("_labels"));
    }

    /**
     * 子查询从表更新
     */
    @Test
    public void test02() {
        DataSource ds = DatasourceConfig.DATA_SOURCES.get("defaultDS");
        Common.sqlExe(ds, "update label set label='aa' where id=1");

        Dml dml = new Dml();
        dml.setDestination("example");
        dml.setTs(new Date().getTime());
        dml.setType("UPDATE");
        dml.setDatabase("mytest");
        dml.setTable("label");
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> data = new LinkedHashMap<>();
        dataList.add(data);
        data.put("id", 1L);
        data.put("user_id", 1L);
        data.put("label", "aa");
        dml.setData(dataList);

        List<Map<String, Object>> oldList = new ArrayList<>();
        Map<String, Object> old = new LinkedHashMap<>();
        oldList.add(old);
        old.put("label", "a");
        dml.setOld(oldList);

        String database = dml.getDatabase();
        String table = dml.getTable();
        Map<String, ESSyncConfig> esSyncConfigs = esAdapter.getDbTableEsSyncConfig().get(database + "-" + table);

        esAdapter.getEsSyncService().sync(esSyncConfigs.values(), dml);

        GetResponse response = esAdapter.getTransportClient().prepareGet("mytest_user", "_doc", "1").get();
        Assert.assertEquals("b;aa", response.getSource().get("_labels"));
    }

    /**
     * 子查询从表删除
     */
    @Test
    public void test03() {
        DataSource ds = DatasourceConfig.DATA_SOURCES.get("defaultDS");
        Common.sqlExe(ds, "delete from label where id=1");

        Dml dml = new Dml();
        dml.setDestination("example");
        dml.setTs(new Date().getTime());
        dml.setType("DELETE");
        dml.setDatabase("mytest");
        dml.setTable("label");
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> data = new LinkedHashMap<>();
        dataList.add(data);
        data.put("id", 1L);
        data.put("user_id", 1L);
        data.put("label", "a");
        dml.setData(dataList);

        String database = dml.getDatabase();
        String table = dml.getTable();
        Map<String, ESSyncConfig> esSyncConfigs = esAdapter.getDbTableEsSyncConfig().get(database + "-" + table);

        esAdapter.getEsSyncService().sync(esSyncConfigs.values(), dml);

        GetResponse response = esAdapter.getTransportClient().prepareGet("mytest_user", "_doc", "1").get();
        Assert.assertEquals("b", response.getSource().get("_labels"));
    }
}
