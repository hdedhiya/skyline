import com.github.davidmoten.rtree.geometry.Point;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SkylineTest {
    private static void saveSkyline(BBSkyline skyline, File file) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));

        for (Point p: skyline.getSkyline()){
            bw.write(String.format("%.2f %.2f\n", p.x(), p.y()));
        }

        bw.flush();
        bw.close();
    }

    private static File makeSFile(String PWD) {
        final String sResultPathFormat =  PWD + "/" + "target/skyline/skyline%s.txt";
        String sResultPath = String.format(sResultPathFormat, "");
        File sResult = new File(sResultPath);

        // Loop until found non-duplicate filename
        int i = 1;
        while (sResult.exists()) {
            sResultPath = String.format(sResultPathFormat, "-"+i++);
            sResult = new File(sResultPath);
        }

        return sResult;
    }

    public static void main(String[] args) throws IOException {
        final String PWD = new File("./").getCanonicalPath();

        // Create test result directory if does not exist
        File resultDir = new File(PWD + "/" + "target/skyline");

        if (!resultDir.exists())
            resultDir.mkdirs();
        else {
            if (!resultDir.isDirectory()) {
                System.err.println("Error accessing test result directory");
                System.exit(-1);
            }
        }

        BBSkyline bbs = new BBSkyline();
        saveSkyline(bbs, makeSFile(PWD));
        // bbs.insert(38.2, 24.8);
        bbs.delete(34.78, 19.24, true);
        saveSkyline(bbs, makeSFile(PWD));
    }
}
