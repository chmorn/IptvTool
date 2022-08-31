import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author chmorn
 * @className Test
 * @description TODO
 * @date 2022/8/31
 **/
public class Test {
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-M-d H:m");
    private static DateTimeFormatter dateTimeFormatter2 = DateTimeFormatter.ofPattern("yyyy-M-d H:m:s");

    public static void main(String[] args) throws IOException {
        System.out.println(File.separator);
    }

    // ts视频合并+（new文件夹）
    public static void tsMix() throws IOException {
        System.out.println("开始合并.............");
        String filepath = "D:/Download/6617933/";
        File dir = new File(filepath);
        File[] files = dir.listFiles();
        //files读取可能顺序乱了，重新排序
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < files.length; i++) {
            list.add(files[i].getName());
        }
        Collections.sort(list);
        //排序结束
        FileInputStream fis = null;
        FileOutputStream fos = new FileOutputStream(filepath + "merge.ts");
        byte[] buffer = new byte[1024];// 一次读取1K
        int len;
        System.out.println(files.length);
        // 长度减1（有个new文件夹）
        for (int i = 0; i < list.size(); i++) {
            fis = new FileInputStream(new File(filepath + list.get(i)));
            //fis = new FileInputStream(new File(filepath+i+".ts"));
            len = 0;
            while ((len = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);// buffer从指定字节数组写入。buffer:数据中的起始偏移量,len:写入的字数。
            }
            fis.close();
        }
        fos.flush();
        fos.close();
        System.out.println("合并完成.............");

    }
}
