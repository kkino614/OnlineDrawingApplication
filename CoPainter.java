import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

class Path implements Serializable{
	private static final transient long serialVersionUID = 5147133119115649618L;
	public ArrayList<Point> point = new ArrayList<Point>();
	public Color color;
	public int pensize;

	public Path(ArrayList<Point> p, Color c,int pz){
		point.addAll(p);
		color = c;
		pensize = pz;

	}
}
class ObjectStream{
	public ObjectOutputStream os;
	public ObjectInputStream is;
	public ObjectStream(ObjectOutputStream o, ObjectInputStream i){
		os = o;
		is = i;
	}
}
class ObjectIOStream extends ArrayList<ObjectStream> {
	public synchronized boolean add(ObjectStream ojs) {  return super.add(ojs);  }
	public synchronized boolean remove(ObjectStream ojs) {  return super.remove(ojs);  }
}
public class CoPainter{
	int clientID;
	ObjectIOStream clientStream = new ObjectIOStream();
	JFrame frame = new JFrame();
	MyPanel DrawPanel = new MyPanel();
	JPanel MainPanel = new JPanel();
	JPanel ChooseColorPanel = new JPanel();
	JPanel ChooseSizePanel = new JPanel();
	Color color = Color.black;
	boolean host_client = false;
	Path inputpath;

	boolean canrun;
	ObjectInputStream is;
	ObjectOutputStream os;
	
	public String IP;
	public String PortString;
	public int Port;
	
	static int clientNo=0;
	int PenSize = 20;
	JMenuBar menubar = new JMenuBar();
	JMenu control = new JMenu("Control");
	JMenuItem clear = new JMenuItem("Clear");
	JMenuItem save = new JMenuItem("Save");
	JMenuItem load = new JMenuItem("Load");
	JMenuItem exit = new JMenuItem("Exit");

	Socket s;
	ServerSocket ss;
	
	ArrayList<Path> path = new ArrayList<Path>();
	ArrayList<Point> points = new ArrayList<Point>();
	ArrayList<Point> points2 = new ArrayList<Point>();
	
	JButton color_red = new JButton("");
	JButton color_green = new JButton();
	JButton color_blue = new JButton();
	JButton color_orange = new JButton();
	JButton color_purple = new JButton();
	JButton color_black = new JButton();
	JButton color_yellow = new JButton();
	JButton color_white = new JButton();
	
	JButton size1 = new JButton("1");
	JButton size2 = new JButton("2");
	JButton size3 = new JButton("3");
	JButton size4 = new JButton("4");
	
	JMenu Help = new JMenu("Help");
	static CoPainter game = new CoPainter();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		game.Get_Info();		
	}
	public void Get_Info(){
		JFrame frame_start = new JFrame();
		JPanel InfoPanel = new JPanel();
		JPanel ChooseHostPanel = new JPanel();
		JPanel ChoosePortPanel = new JPanel();
		JPanel Host_Port = new JPanel();
		JTextField text_host = new JTextField(20);
		JTextField text_port = new JTextField(20);	
		JLabel Host = new JLabel("Host:");
		JLabel Ports = new JLabel("Port:");
		ChooseHostPanel.add(Host);
		ChooseHostPanel.add(text_host);
		ChoosePortPanel.add(Ports);
		ChoosePortPanel.add(text_port);
		JButton Start_as_host = new JButton("Start as a host");
		JButton Connect_to_host = new JButton("Connect to a host");
		Start_as_host.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				host_client = true;
				IP = text_host.getText();
				PortString = text_port.getText();
				Port = Integer.parseInt(PortString);
				frame_start.setVisible(false);
				setupNetworking();
				if(canrun)
					game.Initialize();
				else{
					game.Get_Info();
				}
				
			}
		});
		Connect_to_host.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				host_client = false;
				IP = text_host.getText();
				PortString = text_port.getText();
				Port = Integer.parseInt(PortString);
				frame_start.setVisible(false);
				setupNetworking();
				if(canrun)
					game.Initialize();
				else{
					game.Get_Info();
					//frame_start.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
				}
			}
		});
		Host_Port.add(Start_as_host);
		Host_Port.add(Connect_to_host);
		InfoPanel.setLayout(new GridLayout(3,1));
		InfoPanel.add(ChooseHostPanel);
		InfoPanel.add(ChoosePortPanel);
		InfoPanel.add(Host_Port);
		frame_start.getContentPane().add(InfoPanel);
		frame_start.setTitle("Drawing Game");
		frame_start.setSize(300, 150);
		frame_start.setJMenuBar(menubar);
		frame_start.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame_start.setVisible(true);
	}
	public class ClientHandler implements Runnable {   //Inner class
		ObjectStream ojs;
		Path inputpath;
		Socket sock;
		public ClientHandler(Socket client, ObjectOutputStream los, ObjectInputStream lis) {
			clientID = ++clientNo;
			ojs = new ObjectStream(los, lis);			
			clientStream.add(ojs);
			try {
				sock = client;
				os.reset();
				os.writeObject(path);
				os.flush();
					
			}catch(Exception e){
				 
			}
		}
		public void run() {
			try {
				if(!(host_client && clientNo==0)){
				while(true){
					try{
						Object ob;
						ob = (Object) ojs.is.readObject();
						//System.out.println(inputpath.type);
						if(ob instanceof String){
							clientNo--;
							clientStream.remove(ojs);
							break;
						}
						else {
							inputpath = (Path)ob;
							path.add(inputpath);
							DrawPanel.repaint();
							tellEveryone(inputpath);
						}
					}
					catch(Exception e){ }
					}
				}
				/*while (true) {

					if (message.equals("logout") ) {
						clientStream.remove(w);
						System.out.println( "No. of remaining clients: " +
								clientStream.size());
						return; 

					}
					else {  
						System.out.println("Received: " + message);
						tellEveryone( clientID + ": " + message);
					}
				}*/
			} catch (Exception e) {}
		} 
		public void tellEveryone(Path pa) {
			for (ObjectStream io: clientStream) {
				try {
					io.os.writeObject(pa);
					io.os.flush();
				}catch(Exception e){
					 
				}
			}
		}
	}
	public class Server implements Runnable { //Inner class
		public void run(){
			try {
				while (true) {
					try {
						Socket s = ss.accept();
						is = new ObjectInputStream(s.getInputStream());
						os = new ObjectOutputStream(s.getOutputStream());
						
						Thread t = new Thread(new ClientHandler(s, os, is));
						t.start();
					}
					catch (Exception e) { }
					}

				
				}catch (Exception e) {}
			}
		}
	public class InReader implements Runnable { //Inner class
	public void run(){
		try {
			while (true) {
				try {
					Object ob;
					String s;
					ob = (Object) is.readObject();
					if(ob instanceof String){
						s = (String)ob;
						if(s.equals("String")){
							JOptionPane.showMessageDialog(null, "Host is gone!", "Connection dropped",  JOptionPane.ERROR_MESSAGE, null); 
							System.exit(1);
						}else if(s.equals("clear")){
							path.clear();
							points.clear();
							DrawPanel.repaint();
						}
					}else if(ob instanceof Path){
						inputpath = (Path)ob;
						path.add(inputpath);
						
						DrawPanel.repaint();
					}else{
						ArrayList<Path> inputpath = (ArrayList<Path>) ob;
						for(Path pa: inputpath){
							path.add(pa);
						}
						DrawPanel.repaint();
					}

				}
				catch (Exception e) {  }
				}

			
			}catch (Exception e) {   }
		}
	}
	
	public void setupNetworking(){
		if(host_client == true){
			try{
				ss = new ServerSocket(Port);
				//s = new Socket(IP, Port);
				canrun = true;
				Thread a = new Thread(new Server());
				a.start();
			}catch (IOException e){ 
				canrun=false;
				JOptionPane.showMessageDialog(null, "Unable to listen to port " + Integer.toString(Port), "Failed to start", JOptionPane.ERROR_MESSAGE, null); 
				}
			}
			else if (host_client == false){
				try {
					canrun = true;
					s = new Socket(IP, Port);
					os = new ObjectOutputStream(s.getOutputStream());
					is = new ObjectInputStream(s.getInputStream());
					//clientStream.add(new ObjectStream(os, is));
					Thread t = new Thread(new InReader());
					t.start();
				}catch(IOException e){
					canrun=false;
					JOptionPane.showMessageDialog(null, "Unable to connect to host!", "Failed to start",  JOptionPane.ERROR_MESSAGE, null); 

				}
			}
		}

	public void Initialize() {
		// TODO Auto-generated method stub
		size1.setFont(new Font("Sans Serif", Font.PLAIN, 18));
		size2.setFont(new Font("Sans Serif", Font.PLAIN, 18));
		size3.setFont(new Font("Sans Serif", Font.PLAIN, 18));
		size4.setFont(new Font("Sans Serif", Font.PLAIN, 18));
		
		color_red.setBackground(Color.RED);
		color_orange.setBackground(Color.ORANGE);
		color_yellow.setBackground(Color.YELLOW);
		color_green.setBackground(Color.GREEN);
		color_blue.setBackground(Color.BLUE);
		color_purple.setBackground(Color.MAGENTA);
		color_black.setBackground(Color.BLACK);
		color_white.setBackground(Color.WHITE);
		
		color_red.addActionListener(new ColorListener());
		color_orange.addActionListener(new ColorListener());
		color_yellow.addActionListener(new ColorListener());
		color_green.addActionListener(new ColorListener());
		color_blue.addActionListener(new ColorListener());
		color_purple.addActionListener(new ColorListener());
		color_black.addActionListener(new ColorListener());
		color_white.addActionListener(new ColorListener());
		
		size1.addActionListener(new SizeListener());
		size2.addActionListener(new SizeListener());
		size3.addActionListener(new SizeListener());
		size4.addActionListener(new SizeListener());
		
		ChooseColorPanel.setLayout(new GridLayout(1,12));
		ChooseColorPanel.add(color_red);
		ChooseColorPanel.add(color_orange);
		ChooseColorPanel.add(color_yellow);
		ChooseColorPanel.add(color_green);
		ChooseColorPanel.add(color_blue);
		ChooseColorPanel.add(color_purple);
		ChooseColorPanel.add(color_black);
		ChooseColorPanel.add(color_white);
		
		ChooseColorPanel.add(size4);
		ChooseColorPanel.add(size3);
		ChooseColorPanel.add(size2);
		ChooseColorPanel.add(size1);
		
		DrawPanel.addMouseListener(DrawPanel);
		DrawPanel.addMouseMotionListener(DrawPanel);
		DrawPanel.setBounds(0,0,545,500);
		
		MainPanel.setLayout(new BorderLayout());
		MainPanel.add(DrawPanel);
		MainPanel.add(ChooseColorPanel, BorderLayout.PAGE_END);

		menubar.add(control);
		control.add(save);
		if(host_client){
			control.add(clear);
			clear.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0){
					path.clear();
					points.clear();
					DrawPanel.repaint();
					if(!(host_client && clientNo==0)){
					try{
						os.reset();
						os.writeObject("clear");
						os.flush();
						}
						catch(IOException e){
							 
						}
					}

				}
			});
			control.add(load);
			load.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					// TODO Auto-generated method stub
					try{
							JFileChooser chooser = new JFileChooser();
							int returnVal = chooser.showSaveDialog(frame);
							String filename = chooser.getSelectedFile().getName();
							if (returnVal == JFileChooser.APPROVE_OPTION) {
								FileInputStream filestream = new FileInputStream(filename);
								ObjectInputStream in = new ObjectInputStream(filestream);
								ArrayList<Path> inputpath = (ArrayList<Path>) in.readObject();
								for(Path pa: inputpath){
									path.add(pa);
								}
								DrawPanel.repaint();
								filestream.close();
								in.close();
								if(!(host_client && clientNo==0)){
								for(ObjectStream ob: clientStream){
									try{
										ob.os.reset();
										ob.os.writeObject(path);
										ob.os.flush();
										}
										catch(IOException e){
											 
										}
									}
								}
							}
						}catch (Exception e) { }
				}
			});
		}
		control.add(exit);
		exit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				if(!host_client){
					try{
						os.reset();
						os.writeObject("String");
						os.flush();
						}
						catch(IOException e){
							 
						}
					frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
				}
				else{
					
					for(ObjectStream ob: clientStream){
						try{
							ob.os.reset();
							ob.os.writeObject("String");
							ob.os.flush();
							}
							catch(IOException e){
								 
							}
					}
					frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
				}
			}
		});

		
		save.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				try{
					JFileChooser chooser = new JFileChooser();
					int returnVal = chooser.showSaveDialog(frame);
					String filename = chooser.getSelectedFile().getName();
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						FileOutputStream filestream = new FileOutputStream(filename);
						ObjectOutputStream os = new ObjectOutputStream(filestream);
						os.writeObject(path);
						filestream.close();
						os.close();
					}
					}catch (Exception e) {}
			}
		});
		
		
		//frame.setResizable(false);
		
		

		frame.getContentPane().add(MainPanel);
		
		frame.setTitle("Drawing Game");
		frame.setSize(545, 500);
		frame.setJMenuBar(menubar);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);	

	}
	public class ColorListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource()==color_red)
				color = Color.red;
			else if(e.getSource()==color_orange)
				color = Color.orange;
			else if(e.getSource()==color_yellow)
				color = Color.yellow;
			else if(e.getSource()==color_green)
				color = Color.green;
			else if(e.getSource()==color_blue)
				color = Color.blue;
			else if(e.getSource()==color_purple)
				color = Color.magenta;
			else if(e.getSource()==color_black)
				color = Color.black;
			else if(e.getSource()==color_white)
				color = Color.white;
		}
	}
	public class SizeListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource()==size1)
				PenSize = 10;
			else if(e.getSource()==size2)
				PenSize = 20;
			else if(e.getSource()==size3)
				PenSize = 30;
			else if(e.getSource()==size4)
				PenSize = 40;
				
		}
	}


	public class MyPanel extends JPanel implements MouseListener, MouseMotionListener, Serializable{
		 //An inner class
		protected synchronized void paintComponent(Graphics g){
			try{
			super.paintComponent(g);
			//g.setColor(getBackground());
			//g.fillRect(0, 0, getWidth(), getHeight());
			//g.setColor(getForeground());
			Graphics2D g2D = (Graphics2D) g;
			if(g instanceof Graphics2D){

				g2D.setColor(color);
				g2D.setStroke(new BasicStroke(PenSize, BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
			}
			for (Path pa: path){
				Point prevPoint = null;
				for(Point p : pa.point){
					if(prevPoint != null){
						g2D.setColor(pa.color);
						g2D.setStroke(new BasicStroke(pa.pensize, BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
						g2D.drawLine(prevPoint.x, prevPoint.y, p.x, p.y);
					}
					prevPoint = p;
				}
		 	}
			Point prevPoint = null;
			for(Point p: points){
				if(prevPoint != null){
					g2D.setColor(color);
					g2D.setStroke(new BasicStroke(PenSize, BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
					g2D.drawLine(prevPoint.x, prevPoint.y, p.x, p.y);
						//System.out.print(p.x + "," + p.y);
				}
				prevPoint = p;
			 	
			}
			}catch(Exception e){
				 }
			
			/*Point prevPoint2 = null;
			for (Point p: points2){
			if (prevPoint2 != null){
				g.drawLine(prevPoint2.x, prevPoint2.y, p.x, p.y);
			}
				prevPoint2 = p;
			}*/

		}
		@Override
		public void mouseDragged(MouseEvent event) {
			// TODO Auto-generated method stub
			points.add(event.getPoint());
			repaint();
		}

		@Override
		public void mouseMoved(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public synchronized void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			points.clear();
			points.add(arg0.getPoint());
			repaint();
		}
		@Override
		public synchronized void mouseReleased(MouseEvent arg0) {
			path.add(new Path(points, color, PenSize));
			//System.out.println(path.size());
			// TODO Auto-generated method stub

			if(!(host_client && clientNo==0) && host_client){
				for (ObjectStream io: clientStream) {
				try{
					io.os.reset();
					io.os.writeObject(new Path(points, color, PenSize));
					io.os.flush();
					}
					catch(IOException e){
						 
					}
				}
			}else if(!host_client){
				try{
					os.reset();
					os.writeObject(new Path(points, color, PenSize));
					os.flush();
					}
					catch(IOException e){
						 
			
					}
			}
		}
	}
	

}