package data;

import common.event.BulkHintEvent;
import common.event.HintEvent;
import org.la4j.matrix.SparseMatrix;
import org.la4j.matrix.functor.MatrixProcedure;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * User: hfriedrich
 * Date: 23.06.2015
 */
public class HintReader
{

  public static BulkHintEvent readHints(String folder) throws IOException {

    // read the header file
    ArrayList<String> needHeaders = new ArrayList<>();
    FileInputStream fis = new FileInputStream(folder + "/"+ RescalMatchingData.HEADERS_FILE);
    BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
    String line = null;
    int i = 0;
    while ((line = br.readLine()) != null) {
      if (line.startsWith(RescalMatchingData.NEED_PREFIX)) {
        String originalHeaderEntry = line.substring(RescalMatchingData.NEED_PREFIX.length());
        needHeaders.add(i, originalHeaderEntry);
      }
      i++;
    }
    br.close();

    // read the hint matrix (supposed to contain new hints only, without the connection entries)
    fis = new FileInputStream(folder + "/hints.mtx");
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
    StringBuffer stringBuffer = new StringBuffer();
    line = null;
    while ((line=bufferedReader.readLine())!=null) {
      if (line.startsWith("%%MatrixMarket")) {
        if (!line.contains("row-major") || !line.contains("column-major")) {
          stringBuffer.append(line).append(" row-major\n");
        } else {
          stringBuffer.append(line).append("\n");
        }
      } else if (!line.startsWith("%")) {
        stringBuffer.append(line).append("\n");
      }
    }
    String hintMatrixString = stringBuffer.toString();
    SparseMatrix hintMatrix = SparseMatrix.fromMatrixMarket(hintMatrixString);

    // create the hint events and return them in one bulk hint object
    BulkHintEventMatrixProcedure hintProcedure = new BulkHintEventMatrixProcedure(needHeaders);
    hintMatrix.eachNonZero(hintProcedure);
    return hintProcedure.getBulkHintEvent();
  }

  private static class BulkHintEventMatrixProcedure implements MatrixProcedure
  {
    private BulkHintEvent hints;
    private ArrayList<String> needUris;

    public BulkHintEventMatrixProcedure(ArrayList<String> needUris) {
      hints = new BulkHintEvent();
      this.needUris = needUris;
    }

    @Override
    public void apply(final int i, final int j, final double value) {

      String needUri1 = needUris.get(i);
      String needUri2 = needUris.get(j);
      if (needUri1 != null && needUri2 != null) {
        HintEvent hint = new HintEvent(needUri1, needUri2, value);
        hints.addHintEvent(hint);
      }
    }

    public BulkHintEvent getBulkHintEvent() {
      return hints;
    }
  }
}
