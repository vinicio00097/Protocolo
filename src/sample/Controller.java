package sample;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.jfoenix.controls.*;
import com.jfoenix.controls.cells.editors.TextFieldEditorBuilder;
import com.jfoenix.controls.cells.editors.base.GenericEditableTreeTableCell;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.jfoenix.skins.JFXButtonSkin;
import com.sun.javafx.binding.ExpressionHelper;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.Duration;
import sun.security.ssl.HandshakeInStream;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.*;
import java.util.List;

public class Controller implements Initializable {
    public TextField definedWord;
    public TextArea incomingWord;
    public Button send;
    public JFXTreeTableView<TramaItem> tablaWord=new JFXTreeTableView<>();
    public JFXTreeTableView<TramaItem> tablaWordToSend=new JFXTreeTableView<>();
    public TextArea errorsDisplay;
    public StackPane container;
    public JFXToggleButton onOffServer;
    public JFXToggleButton onOffClient;
    public JFXButton editar;
    private Stack<TramaItem> missingTramas=new Stack<>();
    private Integer maxErrors=5;
    private String word="esto es una prueba para la clase de redes";
    private String generator="1101";
    private List<TramaItem> encodedWord=new ArrayList<>();
    private List<TramaItem> encodedWordToSend=new ArrayList<>();
    private BinaryTransmission centralGroup=new BinaryTransmission();
    private BinaryTransmission groupToSend=new BinaryTransmission();
    private Tooltip tooltip=new Tooltip("No hay nada a enviar, debe contener por lo menos un caracter.");
    private Timeline timeline=new Timeline(new KeyFrame(Duration.millis(3000)));
    private boolean isActive=false;
    private Server server=new Server();
    private Client client=new Client();
    private Kryo kryoServer=server.getKryo();
    private Kryo kryoClient=client.getKryo();
    private HashMap<String,String> headerReceived;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        kryoServer.register(String[].class);
        kryoServer.register(HashMap.class);
        kryoServer.register(Map.class);
        kryoClient.register(String[].class);
        kryoClient.register(HashMap.class);
        kryoClient.register(Map.class);

        send.setText("Enviar");
        definedWord.setText(word);
        errorsDisplay.setFocusTraversable(false);
        errorsDisplay.setStyle("");

        centralGroup.setWord(encodedWord);
        centralGroup.doTransformation(word);
        initTable(tablaWord,encodedWord);

        onOffServer.setOnAction(event -> {
            isActive=!isActive;

            if(isActive){
                try {
                    server.start();
                    server.bind(2000);
                    server.addListener(new Listener(){
                        @Override
                        public void received(Connection connection, Object object) {
                            super.received(connection, object);
                            if(object instanceof String[]){
                                String[] tramaReceived=(String[]) object;
                                System.out.println(tramaReceived[0]+" "+tramaReceived[1]+" "+tramaReceived[2]);
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                server.stop();
            }
        });

        onOffClient.setOnAction(event -> {
            client.start();
            try {
                InetAddress serverAddres=client.discoverHost(5000,2000);

                client.connect(1000,"localhost",2000);
                client.addListener(new Listener(){
                    @Override
                    public void received(Connection connection, Object object) {
                        super.received(connection, object);
                        if(object instanceof String[]){
                            String[] tramaReceived=(String[]) object;
                            List<String> preparedTrama=prepareTramaReceived(tramaReceived[1]);
                            Object[] response=doCRC2(preparedTrama,prepareOperacion(generator,preparedTrama));

                            System.out.println(tramaReceived[0]+" "+tramaReceived[1]+" "+tramaReceived[2]);
                        }else{
                            if(object instanceof HashMap){
                                headerReceived=(HashMap<String, String>) object;
                                System.out.println(headerReceived.get(" "));
                            }else {
                                if(object instanceof String){
                                    switch (headerReceived.get(object.toString())){
                                        case "Inicio":{
                                            errorsDisplay.appendText("Mensaje entrante...\n");
                                        }break;
                                        case "Fin":{
                                            System.out.println("Finalizó");
                                        }break;
                                    }
                                }
                            }
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        editar.setOnAction(event -> {
            JFXButton cancel=new JFXButton("Cancelar");
            JFXButton ok=new JFXButton("Guardar");

            List<Node> actions=new ArrayList<>();
            actions.add(cancel);
            actions.add(ok);

            JFXDialogLayout jfxd = new JFXDialogLayout();
            JFXTextField palabra=new JFXTextField(word);
            jfxd.setHeading(new Text("Modificación de palabra"));
            jfxd.setBody(palabra);
            jfxd.setActions(actions);

            JFXDialog dialog = new JFXDialog(container, jfxd, JFXDialog.DialogTransition.CENTER);
            //dialog.getDialogContainer().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            cancel.setOnAction(event12 -> dialog.close());
            ok.setOnAction(event1 -> {
                if(!palabra.getText().matches("^[\\s]+$")){
                    if(!palabra.getText().isEmpty()){
                        groupToSend.resetAll();
                        centralGroup.resetAll();
                        missingTramas.clear();

                        word=palabra.getText();
                        definedWord.setText(word);

                        centralGroup.setWord(encodedWord);
                        centralGroup.doTransformation(word);
                        initTable(tablaWord,encodedWord);

                        dialog.close();
                    }
                }
            });

            dialog.show();
        });

        send.setOnAction(event -> {

            if(incomingWord.getText().length()>0){
                groupToSend.resetAll();
                missingTramas.clear();
                groupToSend.setWord(encodedWordToSend);
                groupToSend.doTransformation(incomingWord.getText());

                initTableToSend(tablaWordToSend,encodedWordToSend);

                if(validate(encodedWord,encodedWordToSend,0,0)){

                    server.sendToAllTCP(centralGroup.getSpecialOperations());
                    server.sendToAllTCP(centralGroup.getSpecialOperations().get("Inicio"));
                    for(TramaItem tramaItem:encodedWordToSend){
                        System.out.println(tramaItem.getWordIndex());
                        if(!tramaItem.getTramaIndex().equals(" ")){
                            List<String> preparedTrama=prepareTrama(tramaItem.getBinaryLetter());
                            Object[] response=doCRC(preparedTrama,prepareOperacion(generator,preparedTrama));

                            if(Boolean.valueOf(response[0].toString())){
                                tramaItem.setBinaryLetter(tramaItem.getBinaryLetter()+doComplement((ArrayList<String>)response[1]));

                                if(server.getConnections().length>0){
                                    server.sendToAllTCP(new String[]{tramaItem.getWordIndex(),tramaItem.getBinaryLetter(),tramaItem.getTramaIndex()});
                                }
                            }
                        }
                    }

                    server.sendToAllTCP(centralGroup.getSpecialOperations().get("Fin"));
                    errorsDisplay.appendText(incomingWord.getText()+"\n");
                }else{
                    StringBuilder errorMsg=new StringBuilder("Error, tramas faltantes: \n");

                    if(missingTramas.size()>0){
                        for(TramaItem item:missingTramas){
                            errorMsg.append(item.getWordIndex()).append(" ").append(item.getBinaryLetter()).append(" ").append(item.getTramaIndex()).append("\n");
                        }


                        errorsDisplay.appendText(errorMsg.toString());
                    }else{
                        errorsDisplay.appendText("Error, sobrecarga de cadena a enviar. \n");
                    }
                }
            }else{
                tooltip.hide();
                tooltip.setStyle("-fx-background-color:red");
                tooltip.show(send,
                        send.getScene().getWindow().getX()+send.getLayoutX()+send.getWidth(),
                        send.getScene().getWindow().getY()+send.getLayoutY()+send.getHeight()
                );
                timeline.playFromStart();
                timeline.setOnFinished(event1 -> tooltip.hide());
            }
        });
    }


    private void initTable(JFXTreeTableView tabla,List<TramaItem> data){
        //tabla.getItems().clear();
        tabla.getColumns().clear();

        List<String[]> etiquetas=new ArrayList<>();
        etiquetas.add(new String[]{"PN","wordIndex"});
        etiquetas.add(new String[]{"Binario","binaryLetter"});
        etiquetas.add(new String[]{"Letra","letter"});
        etiquetas.add(new String[]{"TN","tramaIndex"});
        tabla.setEditable(false);

        for(String[] item:etiquetas){
            JFXTreeTableColumn<TramaItem,String> column= new JFXTreeTableColumn<>(item[0]);
            //TableColumn column=new TableColumn(item[0]);
            column.setSortable(false);

            //column.setCellFactory((TreeTableColumn<TramaItem, String> param) -> new GenericEditableTreeTableCell<TramaItem,String>(new TextFieldEditorBuilder()));

            //column.setCellValueFactory(new PropertyValueFactory<TramaItem,String>(item[1]));
            column.setCellValueFactory(param -> {
                switch (item[0]){
                    case "PN":{
                        return param.getValue().getValue().wordIndexProperty();
                    }
                    case "Binario":{
                        return param.getValue().getValue().binaryLetterProperty();
                    }
                    case "Letra":{
                        return param.getValue().getValue().letterProperty();
                    }
                    case "TN":{
                        return param.getValue().getValue().tramaIndexProperty();
                    }
                    default:{
                        return param.getValue().getValue().wordIndexProperty();
                    }
                }
            });
            if(item[0].equals("Binario")){
                column.setPrefWidth(90);
            }else{
                column.setPrefWidth(50);
            }
            tabla.getColumns().add(column);
        }

        ObservableList<TramaItem> newItems=FXCollections.observableList(data);
        TreeItem<TramaItem> root = new RecursiveTreeItem<>(newItems, RecursiveTreeObject::getChildren);
        tablaWord.setRoot(root);
        tablaWord.setShowRoot(false);
        //tabla.setItems(FXCollections.observableArrayList(data));
    }

    private void initTableToSend(JFXTreeTableView tabla,List<TramaItem> data){
        tabla.getColumns().clear();

        List<String[]> etiquetas=new ArrayList<>();
        etiquetas.add(new String[]{"PN","wordIndex"});
        etiquetas.add(new String[]{"Binario + Comp","binaryLetter"});
        etiquetas.add(new String[]{"Letra","letter"});
        etiquetas.add(new String[]{"TN","tramaIndex"});
        tabla.setEditable(false);

        for(String[] item:etiquetas){
            JFXTreeTableColumn<TramaItem,String> column= new JFXTreeTableColumn<>(item[0]);
            column.setSortable(false);

            column.setCellValueFactory(param -> {
                switch (item[0]){
                    case "PN":{
                        return param.getValue().getValue().wordIndexProperty();
                    }
                    case "Binario + Comp":{
                        column.setPrefWidth(90);
                        return param.getValue().getValue().binaryLetterProperty();
                    }
                    case "Letra":{
                        return param.getValue().getValue().letterProperty();
                    }
                    case "TN":{
                        return param.getValue().getValue().tramaIndexProperty();
                    }
                    default:{
                        return param.getValue().getValue().wordIndexProperty();
                    }
                }
            });

            tabla.getColumns().add(column);
        }

        ObservableList<TramaItem> newItems=FXCollections.observableList(data);
        TreeItem<TramaItem> root = new RecursiveTreeItem<>(newItems, RecursiveTreeObject::getChildren);
        tablaWordToSend.setRoot(root);
        tablaWordToSend.setShowRoot(false);
        //tabla.setItems(FXCollections.observableArrayList(data));
    }

    private boolean validate(List<TramaItem> centralGroup,List<TramaItem> groupToSend,int index1,int index2){
        if(index2==groupToSend.size()){
            missingTramas.add(centralGroup.get(index1));

            if(missingTramas.size()>=maxErrors){
                return false;
            }
            if(index1<=centralGroup.size()){
                index1++;
            }
        }else{
            if(!centralGroup.get(index1).getBinaryLetter().equals(groupToSend.get(index2).getBinaryLetter())){
                missingTramas.push(centralGroup.get(index1));

                if(missingTramas.size()>=maxErrors){
                    return false;
                }

                if(centralGroup.get(index1+1).getTramaIndex().equals(" ")){
                    index1+=getSpaceAmount(index1+1,0);
                }else{
                    if(index1<=centralGroup.size()){
                        index1++;
                    }
                }
            }else {
                if(index1<=centralGroup.size()){
                    index1++;
                }
                if(index2<=groupToSend.size()){
                    index2++;
                }
            }
        }

        if(index1==centralGroup.size()){
            return index2 == groupToSend.size();
        }else{
            if(index2==groupToSend.size()){
                return validate(centralGroup,groupToSend,index1,index2);
            }else{
                return validate(centralGroup,groupToSend,index1,index2);
            }
        }
    }

    private int getSpaceAmount(int index,int amount){
        if(encodedWord.get(index).getTramaIndex().equals(" ")){
            amount++;
            index++;

            if(index==encodedWord.size()){
                return amount;
            }else{
                return getSpaceAmount(index,amount);
            }
        }else{
            return amount;
        }
    }

    private Object[] doCRC(List<String> trama,List<String> operacion){
        List<String> newOperacion=new ArrayList<>();
        boolean hasOne=false;


        //System.out.println(trama);
        //System.out.println(operacion);

        for(int count=0;count<trama.size();count++){
            if(!trama.get(count).equals("*")){
                if(!operacion.get(count).equals("*")){
                    if(operacion.get(count).equals(trama.get(count))){
                        if(!hasOne){
                            newOperacion.add("*");
                        }else{
                            newOperacion.add("0");
                        }
                    }else{
                        if(!hasOne){
                            hasOne=true;
                        }
                        newOperacion.add("1");
                    }
                }
            }else{
                if(!hasOne){
                    newOperacion.add("*");
                }
            }
        }

        int actualSize=newOperacion.size();
        for(int count=actualSize;count<trama.size();count++){
            newOperacion.add(trama.get(count));
        }

        if(/*!operacion.get(operacion.size() - 1).equals("*")*/getBitsAmount(newOperacion)<generator.length()){
            //System.out.println(trama);
            //System.out.println(newOperacion);
            return new Object[]{true,newOperacion};
        }else{
            return doCRC(newOperacion,prepareOperacion(generator,newOperacion));
        }
    }

    private Object[] doCRC2(List<String> trama,List<String> operacion){
        List<String> newOperacion=new ArrayList<>();
        boolean hasOne=false;


        //System.out.println(trama);
        //System.out.println(operacion);

        for(int count=0;count<trama.size();count++){
            if(!trama.get(count).equals("*")){
                if(!operacion.get(count).equals("*")){
                    if(operacion.get(count).equals(trama.get(count))){
                        if(!hasOne){
                            newOperacion.add("*");
                        }else{
                            newOperacion.add("0");
                        }
                    }else{
                        if(!hasOne){
                            hasOne=true;
                        }
                        newOperacion.add("1");
                    }
                }
            }else{
                if(!hasOne){
                    newOperacion.add("*");
                }
            }
        }

        int actualSize=newOperacion.size();
        for(int count=actualSize;count<trama.size();count++){
            newOperacion.add(trama.get(count));
        }

        if(getBitsAmount(newOperacion)<generator.length()){
            //System.out.println(trama);
            System.out.println(newOperacion);
            return new Object[]{true,newOperacion};
        }else{
            return doCRC2(newOperacion,prepareOperacion(generator,newOperacion));
        }
    }

    public int getBitsAmount(List<String> trama){
        int counter=0;

        for(String item:trama){
            if(!item.equals("*")){
                counter++;
            }
        }

        return counter;
    }

    private List<String> prepareTrama(String trama){
        List<String> arrayTrama=new ArrayList<>();

        for(int count=0;count<trama.length()+(generator.length()-1);count++){
            if(count>=trama.length()){
                arrayTrama.add("0");
            }else{
                arrayTrama.add(String.valueOf(trama.charAt(count)));
            }
        }

        return arrayTrama;
    }

    private List<String> prepareTramaReceived(String trama){
        List<String> arrayTrama=new ArrayList<>();

        for(int count=0;count<trama.length();count++){
            arrayTrama.add(String.valueOf(trama.charAt(count)));
        }

        return arrayTrama;
    }

    private List<String> prepareOperacion(String generator,List<String> trama){
        List<String> preparedGenerator=new ArrayList<>();
        int count2=0;

        for (String aTrama : trama) {
            if (!aTrama.equals("*")) {
                if (count2 < generator.length()) {
                    preparedGenerator.add(String.valueOf(generator.charAt(count2)));
                    count2++;
                } else {
                    preparedGenerator.add("*");
                }
            } else {
                preparedGenerator.add("*");
            }
        }

        return preparedGenerator;
    }

    private String doComplement(List<String> residuo){
        List<String> total=residuo.subList(((residuo.size()-generator.length())+1),residuo.size());
        StringBuilder returnStatement=new StringBuilder();
        for(int count=0;count<total.size();count++){
            if(total.get(count).equals("*")){
                returnStatement.append("0");
            }else{
                returnStatement.append(total.get(count));
            }
        }

        return returnStatement.toString();
    }
}
