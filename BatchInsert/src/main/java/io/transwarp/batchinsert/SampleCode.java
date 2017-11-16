package io.transwarp.batchinsert;

public class SampleCode {
    public static void main(String[] args) {
        SQLOperation so = new SQLOperation();
        so.HyperbaseBatchInsertWithSql("");
        so.HyperbaseBatchInsertWithoutStructRowKey("tableName", "inputPath", null,false);
        so.HyperbaseBatchInsertWithStructRowKey("", "", null, null, true);
        so.Select("");
        so.close();
    }
}
