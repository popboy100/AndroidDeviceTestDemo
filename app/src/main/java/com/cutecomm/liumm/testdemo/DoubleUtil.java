package com.cutecomm.liumm.testdemo;

import android.text.TextUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by 25817 on 2018/6/6.
 */

public class DoubleUtil {

    // 默认除法运算精度
    private static final int DEF_DIV_SCALE = 2;


    //private static Logger logger = Logger.getLogger(DoubleUtil.class);


    public static Double dealNull(Double d1, Double d2) {
        return (d1 == null) ? d2 : d1;
    }


    public static Double dealNull(Object d1, Double d2) {
        return (d1 == null) ? d2 : (Double) d1;
    }

    public static Double dealNull(String d1)throws ValidateException  {
        return getDouble(d1);
    }

    public static Double dealNull(String d1, Double d2) {
        return getDouble(d1,d2);
    }


    public static Double dealNull(Object d1, Object d2) {
        return (d1 == null) ? (Double) d2 : (Double) d1;
    }


    public static Double getDouble(String value) throws ValidateException {
        try {
            return new Double(value);
        } catch (Exception e) {
            //logger.error("getDouble" + e.getMessage());
            throw new ValidateException("Cann't  Convert Double [" + value + "]");
        }
    }


    public static Double getDouble(String value, Double defaultDouble) {
        try {
            if (!TextUtils.isEmpty(value)) {
                return new Double(value);
            }
        } catch (Exception e) {
            //logger.error("getDouble" + e.getMessage());
        }
        return defaultDouble;
    }


    public static Double getDouble(Object value, Double defaultDouble) {
        try {
            if (!TextUtils.isEmpty(value.toString())) {
                return new Double(value.toString());
            }
        } catch (Exception e) {
            //logger.error("getDouble" + e.getMessage());
        }
        return defaultDouble;
    }

    public static String toString(final Double[] l, final String split) {
        StringBuilder bufs = new StringBuilder();
        if (l != null && l.length != 0) {
            String common = "";
            for (Double g : l) {
                bufs.append(common);
                bufs.append(g);
                common = split;
            }
        }
        return bufs.toString();
    }


    public static String toString(List<Double> l, String split) {
        StringBuilder bufs = new StringBuilder();
        if (l != null && !l.isEmpty()) {
            String common = "";
            for (Double g : l) {
                bufs.append(common);
                bufs.append(g);
                common = split;
            }
        }
        return bufs.toString();
    }


    public static List<Double> toList(final String str, final String split) {
        if (str == null) {
            return null;
        }
        StringTokenizer sts = new StringTokenizer(str, split);
        List<Double> args = new ArrayList<Double>(sts.countTokens());
        while (sts.hasMoreElements()) {
            args.add(getDouble(sts.nextElement(), null));
        }
        return args;
    }


    /**
     * 提供精确的加法运算。
     *
     * @param v1
     *            被加数
     * @param v2
     *            加数
     * @return 两个参数的和
     */
    public static double add(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }


    /**
     * 提供精确的减法运算。
     *
     * @param v1
     *            被减数
     * @param v2
     *            减数
     * @return 两个参数的差
     */
    public static double sub(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).doubleValue();
    }


    /**
     * 提供精确的乘法运算。
     *
     * @param v1
     *            被乘数
     * @param v2
     *            乘数
     * @return 两个参数的积
     */


    public static double mul(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2).doubleValue();
    }


    /**
     *
     * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到
     *
     * 小数点以后2位，以后的数字四舍五入。
     *
     * @param v1
     *            被除数
     *
     * @param v2
     *            除数
     *
     * @return 两个参数的商
     *
     */


    public static double div(double v1, double v2) {
        return div(v1, v2, DEF_DIV_SCALE);
    }


    /**
     *
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指
     *
     * 定精度，以后的数字四舍五入。
     *
     * @param v1
     *            被除数
     *
     * @param v2
     *            除数
     *
     * @param scale
     *            表示表示需要精确到小数点以后几位。
     *
     * @return 两个参数的商
     *
     */


    public static double div(double v1, double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();


    }


    /**
     *
     * 提供精确的小数位四舍五入处理。
     *
     * @param v
     *            需要四舍五入的数字
     *
     * @param scale
     *            小数点后保留几位
     *
     * @return 四舍五入后的结果
     *
     */


    public static double round(double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(Double.toString(v));
        BigDecimal one = new BigDecimal(1);
        return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();


    }

    public static Double[] toArray(String str,String split){
        if(str==null){
            return null;
        }
        StringTokenizer sts=new StringTokenizer(str,split);
        Double[] args=new Double[sts.countTokens()];
        for(int i=0;sts.hasMoreElements();i++){
            args[i]=getDouble(sts.nextElement(),null);
        }
        return args;
    }

    public static boolean isEmpty(Double[] str) {
        return str == null || str.length == 0;
    }
}
