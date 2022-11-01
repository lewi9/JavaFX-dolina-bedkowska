import javafx.application.Application;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.canvas.*;
import javafx.scene.effect.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Polygon;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Font;

import javafx.stage.Stage;

import java.io.File;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;

class GraphicElement
{
        public String tagName = null;
        public String fillColor= null;
        public String strokeColor= null;

        public double strokeWidth = 1.0;
        public double[] strokeDash = {0,0};

        public double tX = 0.0;
        public double tY= 0.0;
        public double sX= 1.0;
        public double sY = 1.0;

        public boolean pattern = false;
        public String patterned = null;

        public ArrayList<Double> arrayListX = new ArrayList<Double>();
        public ArrayList<Double> arrayListY = new ArrayList<Double>();

        public boolean getPath(String t)
        {
                //Maybe should be       t.replaceAll("\\s+"," ");
                t.replaceAll("\t"," ");
                t = t.split("z")[0];
                t = t.split("M")[1];

                String fp = null;
                String[] pp = {};

                if(t.split("l").length > 1)
                {
                        fp = t.split("l")[0];
                        pp = (t.split("l")[1]).split(" ");
                }
                else
                        return false;

                this.arrayListX.add(Double.parseDouble(fp.replaceAll("\\s+","").split(",")[0]));
                this.arrayListY.add(Double.parseDouble(fp.replaceAll("\\s+","").split(",")[1]));

                int ii = 0;

                for(String xy : pp)
                {
                        if(xy.replaceAll("\\s+","").split(",")[0] == "") continue;
                        if(xy.replaceAll("\\s+","").split(",")[1] == "") continue;

                        this.arrayListX.add(Double.parseDouble(xy.replaceAll("\\s+","").split(",")[0]) + this.arrayListX.get(ii));
                        this.arrayListY.add(Double.parseDouble(xy.replaceAll("\\s+","").split(",")[1]) + this.arrayListY.get(ii));

                        ++ii;
                }
                return true;
        }

        public void transform(String t)
        {
                String translate = t.split("\\(")[1]; //translate

                translate = translate.split("\\)")[0];

                this.tX = Double.parseDouble(translate.split(",")[0]);
                this.tY= Double.parseDouble(translate.split(",")[1]);

                if(t.split("\\(").length>2)
                {
                        String scale = t.split("\\(")[2]; //scale
                        scale = scale.split("\\)")[0];
                        this.sX= Double.parseDouble(scale.split(",")[0]);
                        this.sY = Double.parseDouble(scale.split(",")[1]);
                }
        }

        public double[] translateX()
        {

                Object[] x = this.arrayListX.toArray();
                double[] arrX = new double[x.length];
                int t;

                for(int i=0; i<x.length; ++i)
                {
                        t = (int)( (double)x[i] + this.tX - (1-this.sX)*(double)x[i] );
                        arrX[i] = (double)t;
                }

                return arrX;

        }

        public double[] translateY()
        {
                Object[] y = this.arrayListY.toArray();
                double[] arrY = new double[y.length];
                int t;

                for(int i=0; i<y.length; ++i)
                {
                        t = (int)( (double)y[i] + this.tY- (1-this.sY)*(double)y[i] );
                        arrY[i] = (double)t;
                }

                return arrY;
        }

        private void copyAtrr(GraphicElement pattern)
        {
                this.fillColor= pattern.fillColor;
                this.strokeColor= pattern.strokeColor;
                this.arrayListX = pattern.arrayListX;
                this.arrayListY = pattern.arrayListY;
                this.strokeWidth = pattern.strokeWidth;
                this.strokeDash = pattern.strokeDash;
        }

        public void findPattern()
        {
                for(GraphicElement pattern: Map_1.arrayListGraphicElement)
                        if(pattern.pattern)
                                if(pattern.tagName.equals(this.patterned))
                                        this.copyAtrr(pattern);
        }

}

class Text_
{
        public int x = 0;
        public int y = 0;
        public String fontWeight = "NORMAL";
        public double fontSize = 14;
        public String content= "";
}

class Handler_1 extends DefaultHandler
{
        String locName;

        boolean flagG = false;
        String name;

        boolean flagSym= false;
        String symName;

        boolean flagT = false;

        GraphicElement gElem;
        Text_ text_;

        @Override
        public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException
        {

                switch(qName)
                {
                        case "svg":
                                return;
                        case "image":
                                return;
                        case "g":
                                //This if should be edited for another xml file
                                if(attributes.getValue("id").equals("mapa_wekt"))
                                        Map_1.opacity = Double.parseDouble(attributes.getValue("opacity"));
                                else
                                {
                                        flagG=true;
                                        name = attributes.getValue("id");
                                }
                                return;
                        case "symbol":
                                flagSym= true;
                                symName = attributes.getValue("id");
                                return;
                        case "text":
                                text_ = new Text_();
                                flagT = true;
                                break;
                        default:
                                gElem = new GraphicElement();
                                if(flagG)
                                        gElem.tagName = name;
                                if(flagSym)
                                {
                                        gElem.pattern = true;
                                        gElem.tagName = symName;
                                }
                }

                for (int i=0; i < attributes.getLength(); i++)
                {
                        locName = attributes.getQName(i);

                        switch (locName)
                        {
                                case "id":
                                        if(!flagG)
                                                gElem.tagName = attributes.getValue(locName);
                                        break;
                                case "fill":
                                        gElem.fillColor= attributes.getValue(locName);
                                        break;
                                case "stroke":
                                        gElem.strokeColor= attributes.getValue(locName);
                                        break;
                                case "stroke-width":
                                        gElem.strokeWidth = Double.parseDouble(attributes.getValue(locName));
                                        break;
                                case "stroke-dasharray":
                                        gElem.strokeDash[0] =  Double.parseDouble(attributes.getValue(locName).split(" ")[0]);
                                        gElem.strokeDash[1] =  Double.parseDouble(attributes.getValue(locName).split(" ")[1]);
                                        break;
                                case "d":
                                        if(!gElem.getPath(attributes.getValue(locName)))
                                                return;
                                        break;
                                case "xlink:href":
                                        gElem.patterned = attributes.getValue("xlink:href").split("#")[1];
                                        break;
                                case "transform":
                                        gElem.transform(attributes.getValue(locName));
                                        break;
                                case "x":
                                        if(!qName.equals("text"))
                                                gElem.tX = Double.parseDouble(attributes.getValue(locName));
                                        else
                                                text_.x = Integer.parseInt(attributes.getValue(locName));
                                        break;
                                case "y":
                                        if(!qName.equals("text"))
                                                gElem.tY= Double.parseDouble(attributes.getValue(locName));
                                        else
                                                text_.y = Integer.parseInt(attributes.getValue(locName));
                                        break;
                                case "font-weight":
                                        text_.fontWeight = attributes.getValue(locName);
                                        break;
                                case "font-size":
                                        text_.fontSize = Double.parseDouble(attributes.getValue(locName));
                                        break;
                                default:
                                        System.out.println("Unhandled parameter: " + locName);
                        }
                }

                if(!flagT)
                        Map_1.arrayListGraphicElement.add(gElem);
                else
                        Map_1.arrayListText.add(text_);
        }

        @Override
        public void characters(char ch[], int start, int length) throws SAXException
        {
                if(flagT)
                {
                        text_ = Map_1.arrayListText.get(Map_1.arrayListText.toArray().length-1);
                        text_.content = new String(ch, start, length);
                        Map_1.arrayListText.set(Map_1.arrayListText.toArray().length-1, text_);
                }

        }

        @Override
        public void endElement(String uri,String localName,String qName) throws SAXException
        {
                switch(qName)
                {
                        case "g":
                                flagG = false;
                                break;
                        case "symbol":
                                flagSym= false;
                                break;
                        case "text":
                                flagT = false;
                                break;
                }
        }
}


public class Map_1 extends Application
{
        private static final int FRAME_WIDTH  = 640;
        private static final int FRAME_HEIGHT = 480;

        public static double opacity;
        public static ArrayList<GraphicElement> arrayListGraphicElement = new ArrayList<GraphicElement>();
        public static ArrayList<Text_> arrayListText = new ArrayList<Text_>();

        int x, y;

        GraphicsContext gc;
        Canvas canvas;

        private boolean flag_lasy = true;
        private boolean flag_skaly = true;
        private boolean flag_poziomice = true;
        private boolean flag_potok = true;
        private boolean flag_budynki = true;
        private boolean flag_drogi = true;
        private boolean flag_text = true;

        Image image = new Image("map.jpg");

        public static void main(String[] args)
        {
                launch(args);
        }

        @Override
        public void start(Stage primaryStage)
        {

                try
                {
                        File inputFile = new File("points.xml");
                        SAXParserFactory factory = SAXParserFactory.newInstance();
                        SAXParser saxParser = factory.newSAXParser();

                        Handler_1 handler_1 = new Handler_1();

                        saxParser.parse(inputFile, handler_1);
                }
                catch (Exception e)
                {
                        e.printStackTrace();
                }

                AnchorPane root = new AnchorPane();
                primaryStage.setTitle("Map");

                canvas = new Canvas(FRAME_WIDTH, FRAME_HEIGHT);
                canvas.setOnMousePressed(this::mouse);

                gc = canvas.getGraphicsContext2D();

                root.getChildren().add(canvas);

                RadioButton rbtn1 = new RadioButton();
                rbtn1.setText("Woods");
                rbtn1.setSelected(true);
                rbtn1.setOnAction(this::woods);

                root.getChildren().add(rbtn1);
                AnchorPane.setBottomAnchor( rbtn1, 5.0d );
                AnchorPane.setLeftAnchor( rbtn1, 50.0d );

                RadioButton rbtn2 = new RadioButton();
                rbtn2.setText("Rocks");
                rbtn2.setSelected(true);
                rbtn2.setOnAction(this::rocks);

                root.getChildren().add(rbtn2);
                AnchorPane.setBottomAnchor( rbtn2, 5.0d );
                AnchorPane.setLeftAnchor( rbtn2, 150.0d );

                RadioButton rbtn3 = new RadioButton();
                rbtn3.setText("Levels");
                rbtn3.setSelected(true);
                rbtn3.setOnAction(this::levels);

                root.getChildren().add(rbtn3);
                AnchorPane.setBottomAnchor( rbtn3, 5.0d );
                AnchorPane.setLeftAnchor( rbtn3, 250.0d );

                RadioButton rbtn4 = new RadioButton();
                rbtn4.setText("River");
                rbtn4.setSelected(true);
                rbtn4.setOnAction(this::river);

                root.getChildren().add(rbtn4);
                AnchorPane.setBottomAnchor( rbtn4, 5.0d );
                AnchorPane.setLeftAnchor( rbtn4, 350.0d );

                RadioButton rbtn5 = new RadioButton();
                rbtn5.setText("Buildings");
                rbtn5.setSelected(true);
                rbtn5.setOnAction(this::buildings);

                root.getChildren().add(rbtn5);
                AnchorPane.setBottomAnchor( rbtn5, 5.0d );
                AnchorPane.setLeftAnchor( rbtn5, 450.0d );

                RadioButton rbtn6 = new RadioButton();
                rbtn6.setText("Roads");
                rbtn6.setSelected(true);
                rbtn6.setOnAction(this::roads);

                root.getChildren().add(rbtn6);
                AnchorPane.setBottomAnchor( rbtn6, 5.0d );
                AnchorPane.setLeftAnchor( rbtn6, 550.0d );

                RadioButton rbtn7 = new RadioButton();
                rbtn7.setText("Text");
                rbtn7.setSelected(true);
                rbtn7.setOnAction(this::text);

                root.getChildren().add(rbtn7);
                AnchorPane.setBottomAnchor( rbtn7, 25.0d );
                AnchorPane.setLeftAnchor( rbtn7, 50.0d );

                Scene scene = new Scene(root);
                primaryStage.setTitle("Dolina B\u0229dkowska");
                primaryStage.setScene( scene );
                primaryStage.setWidth(FRAME_WIDTH + 10);
                primaryStage.setHeight(FRAME_HEIGHT+ 80);

                draw_scene();

                primaryStage.show();
        }

        private void woods(ActionEvent e)
        {
                flag_lasy = !flag_lasy;
                draw_scene();
        }

        private void rocks(ActionEvent e)
        {
                flag_skaly = !flag_skaly;
                draw_scene();
        }

        private void levels(ActionEvent e)
        {
                flag_poziomice = !flag_poziomice;
                draw_scene();
        }

        private void river(ActionEvent e)
        {
                flag_potok = !flag_potok;
                draw_scene();
        }

        private void buildings(ActionEvent e)
        {
                flag_budynki = !flag_budynki;
                draw_scene();
        }

        private void roads(ActionEvent e)
        {
                flag_drogi = !flag_drogi;
                draw_scene();
        }

        private void text(ActionEvent e)
        {
                flag_text = !flag_text;
                draw_scene();
        }


        private void mouse(MouseEvent e)
        {
                System.out.println("X=" + e.getX());
                System.out.println("Y=" + e.getY());
        }

        private void draw_woods()
        {
                draw_procedure("lasy", true);
        }

        private void draw_rocks()
        {
                draw_procedure("skaly", false);
        }

        private void draw_potok()
        {
                draw_procedure("potok", false);
        }

        private void draw_poziomice()
        {
                draw_procedure("poziomice", false);
        }

        private void draw_budynki()
        {
                draw_procedure("budynki", false);
        }

        private void draw_drogi()
        {
                draw_procedure("drogi", false);
        }

        private void draw_procedure(String s, boolean t)
        {
                for(GraphicElement gElem : Map_1.arrayListGraphicElement)
                {
                        if(gElem.pattern)
                                continue;
                        if(t && (gElem.tagName == null || gElem.tagName.equals(s)))
                        {
                                if(gElem.patterned != null)
                                        gElem.findPattern();
                                draw_object(gElem);
                                continue;
                        }
                        if(gElem.tagName == null)
                                continue;
                        if(gElem.tagName.equals(s))
                        {
                                if(gElem.patterned != null)
                                        gElem.findPattern();

                                draw_object(gElem);
                        }
                }
        }

        private void draw_object(GraphicElement gElem)
        {
                double[] x= gElem.translateX();
                double[] y = gElem.translateY();

                gc.setLineWidth(gElem.strokeWidth);
                gc.setLineDashes(gElem.strokeDash);

                if(gElem.fillColor!= null && !gElem.fillColor.equals("none") )
                {
                        gc.setFill(Color.valueOf(gElem.fillColor));
                        gc.fillPolygon( x, y , x.length);

                        if(gElem.strokeColor!= null && !gElem.strokeColor.equals("none"))
                        {
                                gc.setStroke(Color.valueOf(gElem.strokeColor));
                                gc.strokePolygon(x, y, x.length);
                        }

                        return;
                }

                if(gElem.strokeColor!= null && !gElem.strokeColor.equals("none"))
                {
                        gc.setStroke(Color.valueOf(gElem.strokeColor));
                        gc.strokePolyline(x,y,x.length);
                }
        }

        private void render_text()
        {
                gc.setFill(Color.BLACK);
                for(Text_ text_ : Map_1.arrayListText)
                {
                        Font font = Font.font("Serif",FontWeight.valueOf(text_.fontWeight.toUpperCase()), text_.fontSize);
                        gc.setFont(font);
                        gc.fillText(text_.content, text_.x, text_.y);
                }
        }

        private void draw_scene()
        {
                gc.setGlobalAlpha(1);
                gc.clearRect(0,0,canvas.getWidth(),canvas.getHeight());
                gc.drawImage(image, 0, 0, FRAME_WIDTH, FRAME_HEIGHT);
                gc.setGlobalAlpha(Map_1.opacity);
                if(flag_lasy)
                        draw_woods();
                if(flag_skaly)
                        draw_rocks();
                if(flag_potok)
                        draw_potok();
                if(flag_poziomice)
                        draw_poziomice();
                if(flag_budynki)
                        draw_budynki();
                if(flag_drogi)
                        draw_drogi();
                if(flag_text)
                        render_text();
        }
}	
