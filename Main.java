package application;
	
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.TilePane;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 
 * @author Cheyenne Barton
 *
 */

public class Main extends Application {

	public static HashMap<String, Integer> freq = new HashMap<>();
	Button b;
	
	//create a textarea
	static TextArea text = new TextArea();
	
	@Override
	public void start(Stage Stage) {
		try {
			//set title for the stage
			Stage.setTitle("Word Occurence for the Raven Poem");
			
			//create a label
			Label L = new Label("Top 20 words that occur in the Raven Poem");
			
			//create a button
			b = new Button("Click for Results");
			
			//create a stack pane
			TilePane p = new TilePane();
		
			//add label
			p.getChildren().add(L);
			
			//add button
			p.getChildren().add(b);
			
			//add textarea
			p.getChildren().add(text);
			
			//action event
			EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>() {  
			
	            public void handle(ActionEvent event)
	            {
	            	if(event.getSource() ==b) 
	            	{
	            		//getting path to the file
	                	Path path = Paths.get("ravenpoem.txt");
	                	
	                	try {
	                		//put the file into a string
	                		String poem = Files.readString((path));
	                		
	                		//convert string to lowercase
	                		poem = poem.toLowerCase();
	                		
	                		//use Regex to only keep letters
	              	      	Pattern p = Pattern.compile("[a-z]+");
	              	      	Matcher m = p.matcher(poem);

	              	      	//each call to find() will find the next word in the string
	              	      	while (m.find()) {
	              	      		//seperated and formatted words are put into a string
	              	      		String word = m.group();	      		
	              	        
	              	        	//get the words
	              	            Integer f = freq.get(word);
	              	          
	              	            //if word is found
	              	            if  (f == null)
	              	            {
	              	            	freq.put(word, 1);
	              	            } 
	              	        //if same word is found, increase count
	              	            else 
	              	            {
	              	            	freq.put(word, f+1);
	              	            }
	              	      	}
	                	} catch (IOException xIo) {
	                        xIo.printStackTrace();
	                	}
	                	
	                	try {
	                		
	                		freq.clear();
	                		//create database connection
	                		//connect to MySQL database
	                		String url = "jdbc:mysql://localhost:3306/wordOccurrences";
	                		String username = "root";
	                		String password = "passssssss!1";
	                		Connection con = DriverManager.getConnection(url, username, password);
	                		
	                		// create query to store the hashmap data
	                		String sqlQuery = "Insert INTO wordOccurrences.word(word,value) values(?,?)";
	                		PreparedStatement stmt = con.prepareStatement(sqlQuery);
	                		
	                		//loop through the hashmap and insert each of the key/value pairs
	                		for (Map.Entry<String, Integer> entry : freq.entrySet()) {
	                			String word = entry.getKey();
	                			int value = entry.getValue();
	                			
	                			//set parameter values from the preparedstatement
	                			stmt.setString(1, word);
	                			stmt.setInt(2, value);
	                			
	                			//insert the data to MySQL from preparedstatement
	                			stmt.executeUpdate();
	                		}
	                		
	                		//create query to put hashmap data
	                		String sql = "SELECT * FROM wordOccurrences.word ORDER BY value DESC LIMIT 20";
	                		Statement st = con.createStatement();
	                		ResultSet rs = st.executeQuery(sql);
	                		
	                		//build result string 
	                		StringBuilder result = new StringBuilder();
	                		
	                		//loop through tje resultset and append data to string builder
	                		while (rs.next()) {
	                			String words = rs.getString("word");
	                			int values = rs.getInt("value");
	                			result.append(words).append(" = ").append(values).append("\n");
	                		}
	                		
	                		//set the text from the GUI
	                		text.setText(result.toString());

	                		//close the connection to database
	                		con.close();
	                		
	                	}catch (SQLException e){
	                		e.printStackTrace();
	                	}
	        		}
	            }
			};
			
			//when button is pressed
			b.setOnAction(handler);
			
			//setting up the scence
			Scene scene = new Scene(p,300,550);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			Stage.setScene(scene);
			Stage.show();
			
		} catch(Exception e) {
			e.printStackTrace();
		}	
	}
	
	public static void main(String[] args) throws SQLException, ClassNotFoundException{
		//launch the application
		launch(args);
}
}
