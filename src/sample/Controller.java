package sample;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import java.io.File;
import java.io.IOException;
import bzip2.BZip2OutputStream;
import bzip2.BZip2InputStream;
import java.awt.Desktop;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;

public class Controller {
    private Desktop desktop = Desktop.getDesktop();
    // Size to write in memory while compressing (in bytes)
    private static final int COMPRESSION_CACHE = 10000000;

    // Size to write in memory while decompressing (in bytes)
    private static final int DECOMPRESSION_CACHE = 10000000;

    @FXML
    private Button buttonZip;

    @FXML
    private Button buttonUnzip;

    @FXML
    public void initialize(){
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select PDF files");


        buttonZip.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                File file = fileChooser.showOpenDialog(new Stage());

                if (file != null) {
                    System.out.println(file.getPath());
                    compress(file);
                }
            }
        });

        buttonUnzip.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                File file = fileChooser.showOpenDialog(new Stage());

                if (file != null) {
                    System.out.println(file.getPath());
                    decompress(file);
                }
            }
        });
    }

    public void compress(File fileToArchive) {
        try {


            BufferedInputStream input = new BufferedInputStream(new FileInputStream(fileToArchive));

            File archivedFile = new File(fileToArchive.getPath() + ".bz2");
            archivedFile.createNewFile();

            FileOutputStream fos = new FileOutputStream(archivedFile);
            BufferedOutputStream bufStr = new BufferedOutputStream(fos);

            fos.write("BZ".getBytes());
            BZip2OutputStream bzip2 = new BZip2OutputStream(bufStr);

            while (input.available() > 0) {
                int size = COMPRESSION_CACHE;

                if (input.available() < COMPRESSION_CACHE) {
                    size = input.available();
                }
                byte[] bytes = new byte[size];

                input.read(bytes);

                bzip2.write(bytes);
            }
            bzip2.close();
            bufStr.close();
            fos.close();
            input.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void decompress(File fileToUnArchive) {
        try {
            File unarchived = new File(fileToUnArchive.getPath().replace(".bz2", ""));
            unarchived.createNewFile();

            BufferedInputStream inputStr = new BufferedInputStream(new FileInputStream(fileToUnArchive));

            // read bzip2 prefix
            inputStr.read();
            inputStr.read();

            BufferedInputStream buffStr = new BufferedInputStream(inputStr);

            BZip2InputStream input = new BZip2InputStream(buffStr);

            FileOutputStream outStr = new FileOutputStream(unarchived);

            while (true) {
                byte[] compressedBytes = new byte[DECOMPRESSION_CACHE];

                int byteRead = input.read(compressedBytes);

                outStr.write(compressedBytes, 0, byteRead);
                if (byteRead != DECOMPRESSION_CACHE) {
                    break;
                }
            }

            input.close();
            buffStr.close();
            inputStr.close();
            outStr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}