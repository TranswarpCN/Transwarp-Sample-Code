package io.transwarp.inceptorudaf;

import org.apache.hadoop.hive.ql.exec.UDAF;
import org.apache.hadoop.hive.ql.exec.UDAFEvaluator;

public class InceptorUDAF extends UDAF {
    /*
     * 在实现inceptor自定UDAF时，必须要继承UDAF类，还有在这个类的内部实现一个内部类实现接口UDAFEvaluator
     * 并重写里面的四个主要的方法
     *
     * 1. Iterate函数用于聚合。当每一个新的值被聚合时，此函数被调用。
     * 2. TerminatePartial函数在部分聚合完成后被调用。当hive希望得到部分记录的聚合结果时，此函数被调用。
     * 3. Merge函数用于合并先前得到的部分聚合结果（也可以理解为分块记录的聚合结果）。
     * 4. Terminate返回最终的聚合结果。
     *
     * 下面代码的逻辑要实现的是：
     * 1.保存fields3的第一次出现的任何值
     * 2.保存fields4的最后一个值
     * 3.保存fields5的最后一个非空非null的有效值
     * 4.将fields6的所有非空非null的有效值使用分隔符连接起来
     * 5.返回结果是“第一次出现的fields3，最后一次出现的fields4，fields5的最后一个非空非null的有效值，fields6所有的非空
     * 非null的有效值使用分隔符连接起来的字符串”
     */
    public static class GroupByUDAFEvaluator implements UDAFEvaluator{
        // 一个bean类记录字段和分隔符信息
        public static class PartialResult{
            String filed3;
            String filed4;
            String filed5;
            String filed6;
            String delimiter;
        }

        private PartialResult partial;

        public void init() {
            partial = null;
        }
        /*
         * 需要实现的第一个方法
         * 每个需要处理的处理都要经过的处理
         */
        public boolean iterate(String filed3,String filed4,String filed5 ,String filed6){
            if(filed3 == null && filed4 == null && filed5 == null && filed6 == null){
                return true;
            }

            if(partial == null ){
                partial = new PartialResult();
                partial.filed3 = "";
                partial.filed4 = "";
                partial.filed5 = "";
                partial.filed6 = "";
                partial.delimiter = "-";
                //filed3 logical
                partial.filed3 = filed3;
            }

            //filed4 logical
            partial.filed4 = filed4;
            //filed5 logical
            if(filed5 != null && !filed5.trim().equals("")) {
                partial.filed5 = filed5;
            }
            //filed6 logical
            if(filed6 != null && !filed6.trim().equals("")) {
                if(partial.filed6.length() > 0 ){
                    partial.filed6 = partial.filed6.concat(partial.delimiter);
                }
                partial.filed6 = partial.filed6.concat(filed6);
            }
            return true;
        }

        /*
         * 需要实现的第二个方法
         * 返回部分结果
         */
        public PartialResult terminatePartial(){
            return partial;
        }

        /*
         * 需要实现的第三个方法
         * 将两个部分结果合并
         */
        public boolean merge(PartialResult othe){
            if (othe == null){
                return true;
            }

            if(partial == null ){
                partial = new PartialResult();

                partial.filed3 = othe.filed3;
                partial.filed4 = othe.filed4;
                partial.filed5 = othe.filed5;
                partial.filed6 = othe.filed6;
                partial.delimiter = othe.delimiter;
            } else {
                //filed4 logical
                partial.filed4 = othe.filed4;
                //filed5 logical
                if(othe.filed5 != null && !othe.filed5.trim().equals("")) {
                    partial.filed5 = othe.filed5;
                }
                //filed6 logical
                if(othe.filed6 != null && !othe.filed6.trim().equals("")) {
                    if (partial.filed6.length() >0){
                        partial.filed6 = partial.filed6.concat(othe.delimiter);
                    }
                    partial.filed6 = partial.filed6.concat(othe.filed6);
                }
            }
            return true;
        }

        /*
         * 需要实现的第四个方法
         * 构造返回值类型，返回结果
         */
        public String terminate(){
            return "filed3:"+partial.filed3+" filed4:" +partial.filed4+"  filed5 :"+partial.filed5+" filed6: "+partial.filed6;
        }
    }
}
