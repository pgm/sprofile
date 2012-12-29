package org.github.sprofile;

import org.github.sprofile.io.SnapshotStreamWriter;
import org.itadaki.bzip2.BZip2OutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Profiler2Test {

    static void call1(int count) throws Exception {
        Thread.sleep(100);
        call2(count);
        Thread.sleep(50);
        call3();
        Thread.sleep(100);
    }

    static void call2(int count) throws Exception {
        Thread.sleep(100);
        for (int i = 0; i < count; i++) {
            call4();
        }
        Thread.sleep(100);

    }

    static void call3() throws Exception {
        Thread.sleep(100);
    }

    static void call4() throws Exception {
        Thread.sleep(30);
    }

    static public void main(String args[]) throws Exception {
        System.out.println(new File(".").getAbsolutePath());
        FileOutputStream out = new FileOutputStream("profile2");
        SnapshotStreamWriter writer = new SnapshotStreamWriter("sample proc", out);
        Profiler p = new Profiler(50, writer);
        p.start();

        for (int i = 0; i < 3; i++) {
            FileInputStream in = new FileInputStream("/Users/pgm/search_results.FASTA");
            byte[] buffer = new byte[5000];
            BZip2OutputStream dest = new BZip2OutputStream(new FileOutputStream("/tmp/dump"));
            while (true) {
                int read = in.read(buffer);
                if (read < 0) {
                    break;
                }
                dest.write(buffer, 0, read);
            }
            in.close();
            dest.close();
            System.out.println("" + i);
        }

        p.stop();
        out.close();
    }
}
