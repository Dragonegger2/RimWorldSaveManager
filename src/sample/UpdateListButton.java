package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Vector;

/**
 * Created by tlennon on 11/17/2016.
 */
public class UpdateListButton extends Button {
    ListView<String> listToBeUpdated;
    TextField fieldToBeUpdated;

    public UpdateListButton(ListView<String> listToBeUpdated, TextField fieldToBeUpdated, Window parentStage) {
        this.listToBeUpdated = listToBeUpdated;
        this.fieldToBeUpdated = fieldToBeUpdated;

        this.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                try {
                    //Create directory:
                    File pathToRWSaveFolder = new File(fieldToBeUpdated.getText());
                    if(pathToRWSaveFolder.mkdirs()) {
                        System.out.println("Folder was not present. It has been created.");
                    }

                    directoryChooser.setInitialDirectory(pathToRWSaveFolder);
                    File selectedDirectory = directoryChooser.showDialog(parentStage);
                    fieldToBeUpdated.setText(selectedDirectory.getPath());

                    UpdateList();
                }
                catch(SecurityException e) {
                    System.out.println("Try running this jar file as administrator.");
                }
                catch(IllegalArgumentException e) {
                    System.out.println("Folder does not exist or could not be created.");
                }
                catch(NullPointerException e) {
                    System.out.println("Pressed cancel");
                }
            }
        });
    }

    /**
     * Traverse the file list and update the associated list with the found information.
     */
    public void UpdateList() {
        AddItemsToManagedList(WalkManagedSaveFolder(new File(fieldToBeUpdated.getText())));
    }


    /**
     * Creates a list of files with full path.
     *
     * @param managedSaveFolder
     * @return Collection of files that are going to be displayed and are currently stored in the manaaged save folder.
     */
    private List<File> WalkManagedSaveFolder(File managedSaveFolder) {
        try {
            List<File> managedSaves = new Vector();

            Files.walk(managedSaveFolder.toPath())
                    .filter(Files::isRegularFile)
                    .forEach(
                            (save) -> {
                                if(save.getFileName().toString().contains(".rws"))
                                    managedSaves.add(save.toFile());
                            }
                    );

            //managedSaves.get(0).getName();
            return managedSaves;
        }
        catch (IOException e) {
            System.out.println("ManagedSaveFolder disappeared.");
        }
        return null;
    }

    private void AddItemsToManagedList(List<File> files) {
        files.forEach((file) -> {
            listToBeUpdated.getItems().add(file.getName());
        });
    }

}
