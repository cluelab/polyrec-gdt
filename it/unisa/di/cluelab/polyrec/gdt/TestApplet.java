package it.unisa.di.cluelab.polyrec.gdt;

import it.unisa.di.cluelab.polyrec.Gesture;
import it.unisa.di.cluelab.polyrec.PolyRecognizerGSS;
import it.unisa.di.cluelab.polyrec.Recognizer;
import it.unisa.di.cluelab.polyrec.Result;
import it.unisa.di.cluelab.polyrec.TPoint;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

@SuppressWarnings("serial")
public class TestApplet extends Applet implements MouseListener, MouseMotionListener, ActionListener, ItemListener
{
	static final int GESTURE_PROCESSED = 0;
	static final int STROKE_COMPLETE = 2;
	static final int STROKE_IN_PROGRESS = 1;
	static final String DEFAULT_USER_DEFINED_STRING = "Type name here...";
	int state = GESTURE_PROCESSED;

	Recognizer recognizer;
	Gesture currentGesture = new Gesture();
	Label caption = new Label();
	Button clearCanvas = new Button();
	Checkbox rotInv = new Checkbox("Rotation invariant recognition");
	Button addUserDefined = new Button();
	Button addStandard = new Button();
	TextField userDefinedName = new TextField();
	Choice standardNames = new Choice();  
	String name = "";
	double score = 0;
	Image offScreen;
	Color lineColor;
	Color defaultColor = new Color(0f, 0f, 0f);
	
	
	//param rotInv: invariante rotazione 
	public Recognizer initRecognizer(boolean rotInv){
		recognizer = new PolyRecognizerGSS();
		//aggiungi esempi di gesti
		addSamples();
		return recognizer;
	}
	
	

	@Override
	public void itemStateChanged(ItemEvent e) {
		int cb = e.getStateChange();
		if (cb == 1)
			recognizer = initRecognizer(true);
		else
			recognizer = initRecognizer(false);		
	}

	public void init() 
	{
		//colore random della linea
		lineColor = new Color((float)Math.random(), (float)Math.random(), (float)Math.random());

		recognizer = initRecognizer(false);
		String[] s = recognizer.getClassNames().toArray(new String[0]);
		for(int i = 0; i < s.length; i++)
		{
			standardNames.add(s[i]);
		}

		setLayout(new BorderLayout());
		Panel tempContainer = new Panel();
		tempContainer.setLayout(new BorderLayout());
		tempContainer.add(caption, BorderLayout.CENTER);
		caption.setBackground(Color.YELLOW);
		clearCanvas.setLabel("Clear");
		clearCanvas.addActionListener(this);
		tempContainer.add(clearCanvas, BorderLayout.EAST);
		add(tempContainer, BorderLayout.NORTH);

		tempContainer = new Panel();
		tempContainer.setLayout(new GridLayout(3, 1));

		Panel veryTempContainer = new Panel();
		veryTempContainer.setLayout(new FlowLayout());
		
		//aggiungi nomi dei gesti "standard" alla select/choice
		veryTempContainer.add(new Label("Add as example of existing type:"));
		veryTempContainer.add(standardNames);
		addStandard.setLabel("Add");
		addStandard.setEnabled(false);
		addStandard.addActionListener(this);
		veryTempContainer.add(addStandard);
		tempContainer.add(veryTempContainer);

		//aggiungi text field per inserire il nome di gesture con nomi personalizzati
		veryTempContainer = new Panel();
		veryTempContainer.setLayout(new FlowLayout());
		veryTempContainer.add(new Label("Add as example of custom type:"));
		userDefinedName.setText(DEFAULT_USER_DEFINED_STRING);
		veryTempContainer.add(userDefinedName);
		addUserDefined.setLabel("Add");
		addUserDefined.setEnabled(false);
		addUserDefined.addActionListener(this);
		veryTempContainer.add(addUserDefined);
		tempContainer.add(veryTempContainer);

		veryTempContainer = new Panel();
		veryTempContainer.setLayout(new FlowLayout());
		rotInv.addItemListener(this);
		veryTempContainer.add(rotInv);
		tempContainer.add(veryTempContainer);

		add(tempContainer, BorderLayout.SOUTH);

		resize(400, 400);
		offScreen = createImage(getSize().width, getSize().height);  
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == clearCanvas)
		{
			clearCanvas();
			return;
		}

		if(e.getSource() == addStandard)
		{
			String name = standardNames.getSelectedItem();
			recognizer.addTemplate(name, currentGesture);
			caption.setText("Gesture added as additional version of " + name);
			return;
		}

		if(e.getSource() == addUserDefined)
		{
			String name = userDefinedName.getText();
			if(name.equals(DEFAULT_USER_DEFINED_STRING))
			{
				caption.setText("You must enter a name for the gesture");  
			}
			else
			{
				recognizer.addTemplate(name, currentGesture);
				caption.setText("Gesture added with name " + name);
				userDefinedName.setText(DEFAULT_USER_DEFINED_STRING);
			}

			return;
		}
	}

	private void clearCanvas()
	{
		currentGesture = new Gesture();
		caption.setText("");
		addUserDefined.setEnabled(false);
		addStandard.setEnabled(false);
		repaint();
	}

	public void mouseEntered(MouseEvent e) //mouse entered canvas
	{ }

	public void mouseExited(MouseEvent e) //mouse left canvas
	{ }

	public void mouseClicked(MouseEvent e) //mouse pressed-depressed (no motion in between), if there's motion -> mouseDragged
	{ }

	public void update(MouseEvent e)
	{ 
		TPoint p = new TPoint(e.getX(), e.getY(), e.getWhen());
		currentGesture.addPoint(p);
		repaint();
		e.consume();
	} 

	public void mousePressed(MouseEvent e) 
	{
		int button = e.getButton(); 

		switch(button)
		{
		case MouseEvent.BUTTON1:
		{
			if(state == GESTURE_PROCESSED)
			{
				currentGesture = new Gesture();  
			}

			state = STROKE_IN_PROGRESS;
			caption.setForeground(lineColor);
			caption.setText("Capturing stroke");
			update(e);
			return;
		}

		default:
		{
			return;
		}
		}
	}

	public void mouseReleased(MouseEvent e) 
	{ 
		int button = e.getButton(); 

		switch(button)
		{
		case MouseEvent.BUTTON1:
		{
			state = STROKE_COMPLETE;
			caption.setForeground(lineColor);
			caption.setText("Gesture recorded");
			update(e);
			
			Result r = recognizer.recognize(currentGesture);

			//print polyline angle
/*			PolylineFinder pf = new DouglasPeuckerReducer(currentGesture, PolyRecognizerGSS.params);
			Polyline u = pf.find();
			System.out.println(u.getSlopeChange(1));*/
			
			name = r.getName();
			score = r.getScore();
			caption.setForeground(defaultColor);
			caption.setText("Result: " + name + " (" + round(score, 2) + ")");
			state = GESTURE_PROCESSED;
			addUserDefined.setEnabled(true);
			addStandard.setEnabled(true);
			
			
			return;
		}

		default:
		{
			return;
		}
		}
	}
	public void mouseMoved(MouseEvent e) 
	{  
	}

	public void mouseDragged(MouseEvent e) 
	{   
		state = STROKE_IN_PROGRESS;        
		update(e);
	}

	public void paint(Graphics g)
	{         
		int pointCount = currentGesture.points.size();
		if(pointCount < 2)
		{
			return;
		}

		g.setColor(lineColor);

		for(int i = 0; i < pointCount - 1; i++)
		{
			TPoint p1 = currentGesture.points.get(i);
			TPoint p2 = currentGesture.points.get(i+1);
			g.drawLine((int)p1.getX(), (int)p1.getY(), (int)p2.getX(), (int)p2.getY());
		}
	}

	private double round(double n, double d) // round 'n' to 'd' decimals
	{
		d = Math.pow(10, d);
		return Math.round(n * d) / d;
	}
	
	private void addSamples(){
		Gesture g = new Gesture();
		g.addPoint(new TPoint(137,139,0));g.addPoint(new TPoint(135,141,0));g.addPoint(new TPoint(133,144,0));g.addPoint(new TPoint(132,146,0));g.addPoint(new TPoint(130,149,0));g.addPoint(new TPoint(128,151,0));g.addPoint(new TPoint(126,155,0));g.addPoint(new TPoint(123,160,0));g.addPoint(new TPoint(120,166,0));g.addPoint(new TPoint(116,171,0));g.addPoint(new TPoint(112,177,0));g.addPoint(new TPoint(107,183,0));g.addPoint(new TPoint(102,188,0));g.addPoint(new TPoint(100,191,0));g.addPoint(new TPoint(95,195,0));g.addPoint(new TPoint(90,199,0));g.addPoint(new TPoint(86,203,0));g.addPoint(new TPoint(82,206,0));g.addPoint(new TPoint(80,209,0));g.addPoint(new TPoint(75,213,0));g.addPoint(new TPoint(73,213,0));g.addPoint(new TPoint(70,216,0));g.addPoint(new TPoint(67,219,0));g.addPoint(new TPoint(64,221,0));g.addPoint(new TPoint(61,223,0));g.addPoint(new TPoint(60,225,0));g.addPoint(new TPoint(62,226,0));g.addPoint(new TPoint(65,225,0));g.addPoint(new TPoint(67,226,0));g.addPoint(new TPoint(74,226,0));g.addPoint(new TPoint(77,227,0));g.addPoint(new TPoint(85,229,0));g.addPoint(new TPoint(91,230,0));g.addPoint(new TPoint(99,231,0));g.addPoint(new TPoint(108,232,0));g.addPoint(new TPoint(116,233,0));g.addPoint(new TPoint(125,233,0));g.addPoint(new TPoint(134,234,0));g.addPoint(new TPoint(145,233,0));g.addPoint(new TPoint(153,232,0));g.addPoint(new TPoint(160,233,0));g.addPoint(new TPoint(170,234,0));g.addPoint(new TPoint(177,235,0));g.addPoint(new TPoint(179,236,0));g.addPoint(new TPoint(186,237,0));g.addPoint(new TPoint(193,238,0));g.addPoint(new TPoint(198,239,0));g.addPoint(new TPoint(200,237,0));g.addPoint(new TPoint(202,239,0));g.addPoint(new TPoint(204,238,0));g.addPoint(new TPoint(206,234,0));g.addPoint(new TPoint(205,230,0));g.addPoint(new TPoint(202,222,0));g.addPoint(new TPoint(197,216,0));g.addPoint(new TPoint(192,207,0));g.addPoint(new TPoint(186,198,0));g.addPoint(new TPoint(179,189,0));g.addPoint(new TPoint(174,183,0));g.addPoint(new TPoint(170,178,0));g.addPoint(new TPoint(164,171,0));g.addPoint(new TPoint(161,168,0));g.addPoint(new TPoint(154,160,0));g.addPoint(new TPoint(148,155,0));g.addPoint(new TPoint(143,150,0));g.addPoint(new TPoint(138,148,0));g.addPoint(new TPoint(136,148,0));
		recognizer.addTemplate("triangle", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(87,142,0)); g.addPoint(new TPoint(89,145,0)); g.addPoint(new TPoint(91,148,0)); g.addPoint(new TPoint(93,151,0)); g.addPoint(new TPoint(96,155,0)); g.addPoint(new TPoint(98,157,0)); g.addPoint(new TPoint(100,160,0)); g.addPoint(new TPoint(102,162,0)); g.addPoint(new TPoint(106,167,0)); g.addPoint(new TPoint(108,169,0)); g.addPoint(new TPoint(110,171,0)); g.addPoint(new TPoint(115,177,0)); g.addPoint(new TPoint(119,183,0)); g.addPoint(new TPoint(123,189,0)); g.addPoint(new TPoint(127,193,0)); g.addPoint(new TPoint(129,196,0)); g.addPoint(new TPoint(133,200,0)); g.addPoint(new TPoint(137,206,0)); g.addPoint(new TPoint(140,209,0)); g.addPoint(new TPoint(143,212,0)); g.addPoint(new TPoint(146,215,0)); g.addPoint(new TPoint(151,220,0)); g.addPoint(new TPoint(153,222,0)); g.addPoint(new TPoint(155,223,0)); g.addPoint(new TPoint(157,225,0)); g.addPoint(new TPoint(158,223,0)); g.addPoint(new TPoint(157,218,0)); g.addPoint(new TPoint(155,211,0)); g.addPoint(new TPoint(154,208,0)); g.addPoint(new TPoint(152,200,0)); g.addPoint(new TPoint(150,189,0)); g.addPoint(new TPoint(148,179,0)); g.addPoint(new TPoint(147,170,0)); g.addPoint(new TPoint(147,158,0)); g.addPoint(new TPoint(147,148,0)); g.addPoint(new TPoint(147,141,0)); g.addPoint(new TPoint(147,136,0)); g.addPoint(new TPoint(144,135,0)); g.addPoint(new TPoint(142,137,0)); g.addPoint(new TPoint(140,139,0)); g.addPoint(new TPoint(135,145,0)); g.addPoint(new TPoint(131,152,0)); g.addPoint(new TPoint(124,163,0)); g.addPoint(new TPoint(116,177,0)); g.addPoint(new TPoint(108,191,0)); g.addPoint(new TPoint(100,206,0)); g.addPoint(new TPoint(94,217,0)); g.addPoint(new TPoint(91,222,0)); g.addPoint(new TPoint(89,225,0)); g.addPoint(new TPoint(87,226,0)); g.addPoint(new TPoint(87,224,0));
		recognizer.addTemplate("x", g);
		
		g=new Gesture();
		g.addPoint(new TPoint(78,149,0)); g.addPoint(new TPoint(78,153,0)); g.addPoint(new TPoint(78,157,0)); g.addPoint(new TPoint(78,160,0)); g.addPoint(new TPoint(79,162,0)); g.addPoint(new TPoint(79,164,0)); g.addPoint(new TPoint(79,167,0)); g.addPoint(new TPoint(79,169,0)); g.addPoint(new TPoint(79,173,0)); g.addPoint(new TPoint(79,178,0)); g.addPoint(new TPoint(79,183,0)); g.addPoint(new TPoint(80,189,0)); g.addPoint(new TPoint(80,193,0)); g.addPoint(new TPoint(80,198,0)); g.addPoint(new TPoint(80,202,0)); g.addPoint(new TPoint(81,208,0)); g.addPoint(new TPoint(81,210,0)); g.addPoint(new TPoint(81,216,0)); g.addPoint(new TPoint(82,222,0)); g.addPoint(new TPoint(82,224,0)); g.addPoint(new TPoint(82,227,0)); g.addPoint(new TPoint(83,229,0)); g.addPoint(new TPoint(83,231,0)); g.addPoint(new TPoint(85,230,0)); g.addPoint(new TPoint(88,232,0)); g.addPoint(new TPoint(90,233,0)); g.addPoint(new TPoint(92,232,0)); g.addPoint(new TPoint(94,233,0)); g.addPoint(new TPoint(99,232,0)); g.addPoint(new TPoint(102,233,0)); g.addPoint(new TPoint(106,233,0)); g.addPoint(new TPoint(109,234,0)); g.addPoint(new TPoint(117,235,0)); g.addPoint(new TPoint(123,236,0)); g.addPoint(new TPoint(126,236,0)); g.addPoint(new TPoint(135,237,0)); g.addPoint(new TPoint(142,238,0)); g.addPoint(new TPoint(145,238,0)); g.addPoint(new TPoint(152,238,0)); g.addPoint(new TPoint(154,239,0)); g.addPoint(new TPoint(165,238,0)); g.addPoint(new TPoint(174,237,0)); g.addPoint(new TPoint(179,236,0)); g.addPoint(new TPoint(186,235,0)); g.addPoint(new TPoint(191,235,0)); g.addPoint(new TPoint(195,233,0)); g.addPoint(new TPoint(197,233,0)); g.addPoint(new TPoint(200,233,0)); g.addPoint(new TPoint(201,235,0)); g.addPoint(new TPoint(201,233,0)); g.addPoint(new TPoint(199,231,0)); g.addPoint(new TPoint(198,226,0)); g.addPoint(new TPoint(198,220,0)); g.addPoint(new TPoint(196,207,0)); g.addPoint(new TPoint(195,195,0)); g.addPoint(new TPoint(195,181,0)); g.addPoint(new TPoint(195,173,0)); g.addPoint(new TPoint(195,163,0)); g.addPoint(new TPoint(194,155,0)); g.addPoint(new TPoint(192,145,0)); g.addPoint(new TPoint(192,143,0)); g.addPoint(new TPoint(192,138,0)); g.addPoint(new TPoint(191,135,0)); g.addPoint(new TPoint(191,133,0)); g.addPoint(new TPoint(191,130,0)); g.addPoint(new TPoint(190,128,0)); g.addPoint(new TPoint(188,129,0)); g.addPoint(new TPoint(186,129,0)); g.addPoint(new TPoint(181,132,0)); g.addPoint(new TPoint(173,131,0)); g.addPoint(new TPoint(162,131,0)); g.addPoint(new TPoint(151,132,0)); g.addPoint(new TPoint(149,132,0)); g.addPoint(new TPoint(138,132,0)); g.addPoint(new TPoint(136,132,0)); g.addPoint(new TPoint(122,131,0)); g.addPoint(new TPoint(120,131,0)); g.addPoint(new TPoint(109,130,0)); g.addPoint(new TPoint(107,130,0)); g.addPoint(new TPoint(90,132,0)); g.addPoint(new TPoint(81,133,0)); g.addPoint(new TPoint(76,133,0));
		recognizer.addTemplate("rectangle", g);
		
		g=new Gesture();
		g.addPoint(new TPoint(127,141,0)); g.addPoint(new TPoint(124,140,0)); g.addPoint(new TPoint(120,139,0)); g.addPoint(new TPoint(118,139,0)); g.addPoint(new TPoint(116,139,0)); g.addPoint(new TPoint(111,140,0)); g.addPoint(new TPoint(109,141,0)); g.addPoint(new TPoint(104,144,0)); g.addPoint(new TPoint(100,147,0)); g.addPoint(new TPoint(96,152,0)); g.addPoint(new TPoint(93,157,0)); g.addPoint(new TPoint(90,163,0)); g.addPoint(new TPoint(87,169,0)); g.addPoint(new TPoint(85,175,0)); g.addPoint(new TPoint(83,181,0)); g.addPoint(new TPoint(82,190,0)); g.addPoint(new TPoint(82,195,0)); g.addPoint(new TPoint(83,200,0)); g.addPoint(new TPoint(84,205,0)); g.addPoint(new TPoint(88,213,0)); g.addPoint(new TPoint(91,216,0)); g.addPoint(new TPoint(96,219,0)); g.addPoint(new TPoint(103,222,0)); g.addPoint(new TPoint(108,224,0)); g.addPoint(new TPoint(111,224,0)); g.addPoint(new TPoint(120,224,0)); g.addPoint(new TPoint(133,223,0)); g.addPoint(new TPoint(142,222,0)); g.addPoint(new TPoint(152,218,0)); g.addPoint(new TPoint(160,214,0)); g.addPoint(new TPoint(167,210,0)); g.addPoint(new TPoint(173,204,0)); g.addPoint(new TPoint(178,198,0)); g.addPoint(new TPoint(179,196,0)); g.addPoint(new TPoint(182,188,0)); g.addPoint(new TPoint(182,177,0)); g.addPoint(new TPoint(178,167,0)); g.addPoint(new TPoint(170,150,0)); g.addPoint(new TPoint(163,138,0)); g.addPoint(new TPoint(152,130,0)); g.addPoint(new TPoint(143,129,0)); g.addPoint(new TPoint(140,131,0)); g.addPoint(new TPoint(129,136,0)); g.addPoint(new TPoint(126,139,0));
		recognizer.addTemplate("circle", g);
		
		g=new Gesture();
		g.addPoint(new TPoint(91,185,0)); g.addPoint(new TPoint(93,185,0)); g.addPoint(new TPoint(95,185,0)); g.addPoint(new TPoint(97,185,0)); g.addPoint(new TPoint(100,188,0)); g.addPoint(new TPoint(102,189,0)); g.addPoint(new TPoint(104,190,0)); g.addPoint(new TPoint(106,193,0)); g.addPoint(new TPoint(108,195,0)); g.addPoint(new TPoint(110,198,0)); g.addPoint(new TPoint(112,201,0)); g.addPoint(new TPoint(114,204,0)); g.addPoint(new TPoint(115,207,0)); g.addPoint(new TPoint(117,210,0)); g.addPoint(new TPoint(118,212,0)); g.addPoint(new TPoint(120,214,0)); g.addPoint(new TPoint(121,217,0)); g.addPoint(new TPoint(122,219,0)); g.addPoint(new TPoint(123,222,0)); g.addPoint(new TPoint(124,224,0)); g.addPoint(new TPoint(126,226,0)); g.addPoint(new TPoint(127,229,0)); g.addPoint(new TPoint(129,231,0)); g.addPoint(new TPoint(130,233,0)); g.addPoint(new TPoint(129,231,0)); g.addPoint(new TPoint(129,228,0)); g.addPoint(new TPoint(129,226,0)); g.addPoint(new TPoint(129,224,0)); g.addPoint(new TPoint(129,221,0)); g.addPoint(new TPoint(129,218,0)); g.addPoint(new TPoint(129,212,0)); g.addPoint(new TPoint(129,208,0)); g.addPoint(new TPoint(130,198,0)); g.addPoint(new TPoint(132,189,0)); g.addPoint(new TPoint(134,182,0)); g.addPoint(new TPoint(137,173,0)); g.addPoint(new TPoint(143,164,0)); g.addPoint(new TPoint(147,157,0)); g.addPoint(new TPoint(151,151,0)); g.addPoint(new TPoint(155,144,0)); g.addPoint(new TPoint(161,137,0)); g.addPoint(new TPoint(165,131,0)); g.addPoint(new TPoint(171,122,0)); g.addPoint(new TPoint(174,118,0)); g.addPoint(new TPoint(176,114,0)); g.addPoint(new TPoint(177,112,0)); g.addPoint(new TPoint(177,114,0)); g.addPoint(new TPoint(175,116,0)); g.addPoint(new TPoint(173,118,0));
		recognizer.addTemplate("check", g);
		
		g=new Gesture();
		g.addPoint(new TPoint(79,245,0)); g.addPoint(new TPoint(79,242,0)); g.addPoint(new TPoint(79,239,0)); g.addPoint(new TPoint(80,237,0)); g.addPoint(new TPoint(80,234,0)); g.addPoint(new TPoint(81,232,0)); g.addPoint(new TPoint(82,230,0)); g.addPoint(new TPoint(84,224,0)); g.addPoint(new TPoint(86,220,0)); g.addPoint(new TPoint(86,218,0)); g.addPoint(new TPoint(87,216,0)); g.addPoint(new TPoint(88,213,0)); g.addPoint(new TPoint(90,207,0)); g.addPoint(new TPoint(91,202,0)); g.addPoint(new TPoint(92,200,0)); g.addPoint(new TPoint(93,194,0)); g.addPoint(new TPoint(94,192,0)); g.addPoint(new TPoint(96,189,0)); g.addPoint(new TPoint(97,186,0)); g.addPoint(new TPoint(100,179,0)); g.addPoint(new TPoint(102,173,0)); g.addPoint(new TPoint(105,165,0)); g.addPoint(new TPoint(107,160,0)); g.addPoint(new TPoint(109,158,0)); g.addPoint(new TPoint(112,151,0)); g.addPoint(new TPoint(115,144,0)); g.addPoint(new TPoint(117,139,0)); g.addPoint(new TPoint(119,136,0)); g.addPoint(new TPoint(119,134,0)); g.addPoint(new TPoint(120,132,0)); g.addPoint(new TPoint(121,129,0)); g.addPoint(new TPoint(122,127,0)); g.addPoint(new TPoint(124,125,0)); g.addPoint(new TPoint(126,124,0)); g.addPoint(new TPoint(129,125,0)); g.addPoint(new TPoint(131,127,0)); g.addPoint(new TPoint(132,130,0)); g.addPoint(new TPoint(136,139,0)); g.addPoint(new TPoint(141,154,0)); g.addPoint(new TPoint(145,166,0)); g.addPoint(new TPoint(151,182,0)); g.addPoint(new TPoint(156,193,0)); g.addPoint(new TPoint(157,196,0)); g.addPoint(new TPoint(161,209,0)); g.addPoint(new TPoint(162,211,0)); g.addPoint(new TPoint(167,223,0)); g.addPoint(new TPoint(169,229,0)); g.addPoint(new TPoint(170,231,0)); g.addPoint(new TPoint(173,237,0)); g.addPoint(new TPoint(176,242,0)); g.addPoint(new TPoint(177,244,0)); g.addPoint(new TPoint(179,250,0)); g.addPoint(new TPoint(181,255,0)); g.addPoint(new TPoint(182,257,0));
		recognizer.addTemplate("caret", g);
		
		g=new Gesture();
		g.addPoint(new TPoint(307,216,0)); g.addPoint(new TPoint(333,186,0)); g.addPoint(new TPoint(356,215,0)); g.addPoint(new TPoint(375,186,0)); g.addPoint(new TPoint(399,216,0)); g.addPoint(new TPoint(418,186,0));
		recognizer.addTemplate("zig-zag", g);
		
		g=new Gesture();
		g.addPoint(new TPoint(68,222,0)); g.addPoint(new TPoint(70,220,0)); g.addPoint(new TPoint(73,218,0)); g.addPoint(new TPoint(75,217,0)); g.addPoint(new TPoint(77,215,0)); g.addPoint(new TPoint(80,213,0)); g.addPoint(new TPoint(82,212,0)); g.addPoint(new TPoint(84,210,0)); g.addPoint(new TPoint(87,209,0)); g.addPoint(new TPoint(89,208,0)); g.addPoint(new TPoint(92,206,0)); g.addPoint(new TPoint(95,204,0)); g.addPoint(new TPoint(101,201,0)); g.addPoint(new TPoint(106,198,0)); g.addPoint(new TPoint(112,194,0)); g.addPoint(new TPoint(118,191,0)); g.addPoint(new TPoint(124,187,0)); g.addPoint(new TPoint(127,186,0)); g.addPoint(new TPoint(132,183,0)); g.addPoint(new TPoint(138,181,0)); g.addPoint(new TPoint(141,180,0)); g.addPoint(new TPoint(146,178,0)); g.addPoint(new TPoint(154,173,0)); g.addPoint(new TPoint(159,171,0)); g.addPoint(new TPoint(161,170,0)); g.addPoint(new TPoint(166,167,0)); g.addPoint(new TPoint(168,167,0)); g.addPoint(new TPoint(171,166,0)); g.addPoint(new TPoint(174,164,0)); g.addPoint(new TPoint(177,162,0)); g.addPoint(new TPoint(180,160,0)); g.addPoint(new TPoint(182,158,0)); g.addPoint(new TPoint(183,156,0)); g.addPoint(new TPoint(181,154,0)); g.addPoint(new TPoint(178,153,0)); g.addPoint(new TPoint(171,153,0)); g.addPoint(new TPoint(164,153,0)); g.addPoint(new TPoint(160,153,0)); g.addPoint(new TPoint(150,154,0)); g.addPoint(new TPoint(147,155,0)); g.addPoint(new TPoint(141,157,0)); g.addPoint(new TPoint(137,158,0)); g.addPoint(new TPoint(135,158,0)); g.addPoint(new TPoint(137,158,0)); g.addPoint(new TPoint(140,157,0)); g.addPoint(new TPoint(143,156,0)); g.addPoint(new TPoint(151,154,0)); g.addPoint(new TPoint(160,152,0)); g.addPoint(new TPoint(170,149,0)); g.addPoint(new TPoint(179,147,0)); g.addPoint(new TPoint(185,145,0)); g.addPoint(new TPoint(192,144,0)); g.addPoint(new TPoint(196,144,0)); g.addPoint(new TPoint(198,144,0)); g.addPoint(new TPoint(200,144,0)); g.addPoint(new TPoint(201,147,0)); g.addPoint(new TPoint(199,149,0)); g.addPoint(new TPoint(194,157,0)); g.addPoint(new TPoint(191,160,0)); g.addPoint(new TPoint(186,167,0)); g.addPoint(new TPoint(180,176,0)); g.addPoint(new TPoint(177,179,0)); g.addPoint(new TPoint(171,187,0)); g.addPoint(new TPoint(169,189,0)); g.addPoint(new TPoint(165,194,0)); g.addPoint(new TPoint(164,196,0));
		recognizer.addTemplate("arrow", g);
		
		g=new Gesture();
		g.addPoint(new TPoint(140,124,0)); g.addPoint(new TPoint(138,123,0)); g.addPoint(new TPoint(135,122,0)); g.addPoint(new TPoint(133,123,0)); g.addPoint(new TPoint(130,123,0)); g.addPoint(new TPoint(128,124,0)); g.addPoint(new TPoint(125,125,0)); g.addPoint(new TPoint(122,124,0)); g.addPoint(new TPoint(120,124,0)); g.addPoint(new TPoint(118,124,0)); g.addPoint(new TPoint(116,125,0)); g.addPoint(new TPoint(113,125,0)); g.addPoint(new TPoint(111,125,0)); g.addPoint(new TPoint(108,124,0)); g.addPoint(new TPoint(106,125,0)); g.addPoint(new TPoint(104,125,0)); g.addPoint(new TPoint(102,124,0)); g.addPoint(new TPoint(100,123,0)); g.addPoint(new TPoint(98,123,0)); g.addPoint(new TPoint(95,124,0)); g.addPoint(new TPoint(93,123,0)); g.addPoint(new TPoint(90,124,0)); g.addPoint(new TPoint(88,124,0)); g.addPoint(new TPoint(85,125,0)); g.addPoint(new TPoint(83,126,0)); g.addPoint(new TPoint(81,127,0)); g.addPoint(new TPoint(81,129,0)); g.addPoint(new TPoint(82,131,0)); g.addPoint(new TPoint(82,134,0)); g.addPoint(new TPoint(83,138,0)); g.addPoint(new TPoint(84,141,0)); g.addPoint(new TPoint(84,144,0)); g.addPoint(new TPoint(85,148,0)); g.addPoint(new TPoint(85,151,0)); g.addPoint(new TPoint(86,156,0)); g.addPoint(new TPoint(86,160,0)); g.addPoint(new TPoint(86,164,0)); g.addPoint(new TPoint(86,168,0)); g.addPoint(new TPoint(87,171,0)); g.addPoint(new TPoint(87,175,0)); g.addPoint(new TPoint(87,179,0)); g.addPoint(new TPoint(87,182,0)); g.addPoint(new TPoint(87,186,0)); g.addPoint(new TPoint(88,188,0)); g.addPoint(new TPoint(88,195,0)); g.addPoint(new TPoint(88,198,0)); g.addPoint(new TPoint(88,201,0)); g.addPoint(new TPoint(88,207,0)); g.addPoint(new TPoint(89,211,0)); g.addPoint(new TPoint(89,213,0)); g.addPoint(new TPoint(89,217,0)); g.addPoint(new TPoint(89,222,0)); g.addPoint(new TPoint(88,225,0)); g.addPoint(new TPoint(88,229,0)); g.addPoint(new TPoint(88,231,0)); g.addPoint(new TPoint(88,233,0)); g.addPoint(new TPoint(88,235,0)); g.addPoint(new TPoint(89,237,0)); g.addPoint(new TPoint(89,240,0)); g.addPoint(new TPoint(89,242,0)); g.addPoint(new TPoint(91,241,0)); g.addPoint(new TPoint(94,241,0)); g.addPoint(new TPoint(96,240,0)); g.addPoint(new TPoint(98,239,0)); g.addPoint(new TPoint(105,240,0)); g.addPoint(new TPoint(109,240,0)); g.addPoint(new TPoint(113,239,0)); g.addPoint(new TPoint(116,240,0)); g.addPoint(new TPoint(121,239,0)); g.addPoint(new TPoint(130,240,0)); g.addPoint(new TPoint(136,237,0)); g.addPoint(new TPoint(139,237,0)); g.addPoint(new TPoint(144,238,0)); g.addPoint(new TPoint(151,237,0)); g.addPoint(new TPoint(157,236,0)); g.addPoint(new TPoint(159,237,0));
		recognizer.addTemplate("left square bracket", g);
		
		g=new Gesture();
		g.addPoint(new TPoint(112,138,0)); g.addPoint(new TPoint(112,136,0)); g.addPoint(new TPoint(115,136,0)); g.addPoint(new TPoint(118,137,0)); g.addPoint(new TPoint(120,136,0)); g.addPoint(new TPoint(123,136,0)); g.addPoint(new TPoint(125,136,0)); g.addPoint(new TPoint(128,136,0)); g.addPoint(new TPoint(131,136,0)); g.addPoint(new TPoint(134,135,0)); g.addPoint(new TPoint(137,135,0)); g.addPoint(new TPoint(140,134,0)); g.addPoint(new TPoint(143,133,0)); g.addPoint(new TPoint(145,132,0)); g.addPoint(new TPoint(147,132,0)); g.addPoint(new TPoint(149,132,0)); g.addPoint(new TPoint(152,132,0)); g.addPoint(new TPoint(153,134,0)); g.addPoint(new TPoint(154,137,0)); g.addPoint(new TPoint(155,141,0)); g.addPoint(new TPoint(156,144,0)); g.addPoint(new TPoint(157,152,0)); g.addPoint(new TPoint(158,161,0)); g.addPoint(new TPoint(160,170,0)); g.addPoint(new TPoint(162,182,0)); g.addPoint(new TPoint(164,192,0)); g.addPoint(new TPoint(166,200,0)); g.addPoint(new TPoint(167,209,0)); g.addPoint(new TPoint(168,214,0)); g.addPoint(new TPoint(168,216,0)); g.addPoint(new TPoint(169,221,0)); g.addPoint(new TPoint(169,223,0)); g.addPoint(new TPoint(169,228,0)); g.addPoint(new TPoint(169,231,0)); g.addPoint(new TPoint(166,233,0)); g.addPoint(new TPoint(164,234,0)); g.addPoint(new TPoint(161,235,0)); g.addPoint(new TPoint(155,236,0)); g.addPoint(new TPoint(147,235,0)); g.addPoint(new TPoint(140,233,0)); g.addPoint(new TPoint(131,233,0)); g.addPoint(new TPoint(124,233,0)); g.addPoint(new TPoint(117,235,0)); g.addPoint(new TPoint(114,238,0)); g.addPoint(new TPoint(112,238,0));
		recognizer.addTemplate("right square bracket", g);
		
		g=new Gesture();
		g.addPoint(new TPoint(89,164,0)); g.addPoint(new TPoint(90,162,0)); g.addPoint(new TPoint(92,162,0)); g.addPoint(new TPoint(94,164,0)); g.addPoint(new TPoint(95,166,0)); g.addPoint(new TPoint(96,169,0)); g.addPoint(new TPoint(97,171,0)); g.addPoint(new TPoint(99,175,0)); g.addPoint(new TPoint(101,178,0)); g.addPoint(new TPoint(103,182,0)); g.addPoint(new TPoint(106,189,0)); g.addPoint(new TPoint(108,194,0)); g.addPoint(new TPoint(111,199,0)); g.addPoint(new TPoint(114,204,0)); g.addPoint(new TPoint(117,209,0)); g.addPoint(new TPoint(119,214,0)); g.addPoint(new TPoint(122,218,0)); g.addPoint(new TPoint(124,222,0)); g.addPoint(new TPoint(126,225,0)); g.addPoint(new TPoint(128,228,0)); g.addPoint(new TPoint(130,229,0)); g.addPoint(new TPoint(133,233,0)); g.addPoint(new TPoint(134,236,0)); g.addPoint(new TPoint(136,239,0)); g.addPoint(new TPoint(138,240,0)); g.addPoint(new TPoint(139,242,0)); g.addPoint(new TPoint(140,244,0)); g.addPoint(new TPoint(142,242,0)); g.addPoint(new TPoint(142,240,0)); g.addPoint(new TPoint(142,237,0)); g.addPoint(new TPoint(143,235,0)); g.addPoint(new TPoint(143,233,0)); g.addPoint(new TPoint(145,229,0)); g.addPoint(new TPoint(146,226,0)); g.addPoint(new TPoint(148,217,0)); g.addPoint(new TPoint(149,208,0)); g.addPoint(new TPoint(149,205,0)); g.addPoint(new TPoint(151,196,0)); g.addPoint(new TPoint(151,193,0)); g.addPoint(new TPoint(153,182,0)); g.addPoint(new TPoint(155,172,0)); g.addPoint(new TPoint(157,165,0)); g.addPoint(new TPoint(159,160,0)); g.addPoint(new TPoint(162,155,0)); g.addPoint(new TPoint(164,150,0)); g.addPoint(new TPoint(165,148,0)); g.addPoint(new TPoint(166,146,0));
		recognizer.addTemplate("v", g);
		
		g=new Gesture();
		g.addPoint(new TPoint(123,129,0)); g.addPoint(new TPoint(123,131,0)); g.addPoint(new TPoint(124,133,0)); g.addPoint(new TPoint(125,136,0)); g.addPoint(new TPoint(127,140,0)); g.addPoint(new TPoint(129,142,0)); g.addPoint(new TPoint(133,148,0)); g.addPoint(new TPoint(137,154,0)); g.addPoint(new TPoint(143,158,0)); g.addPoint(new TPoint(145,161,0)); g.addPoint(new TPoint(148,164,0)); g.addPoint(new TPoint(153,170,0)); g.addPoint(new TPoint(158,176,0)); g.addPoint(new TPoint(160,178,0)); g.addPoint(new TPoint(164,183,0)); g.addPoint(new TPoint(168,188,0)); g.addPoint(new TPoint(171,191,0)); g.addPoint(new TPoint(175,196,0)); g.addPoint(new TPoint(178,200,0)); g.addPoint(new TPoint(180,202,0)); g.addPoint(new TPoint(181,205,0)); g.addPoint(new TPoint(184,208,0)); g.addPoint(new TPoint(186,210,0)); g.addPoint(new TPoint(187,213,0)); g.addPoint(new TPoint(188,215,0)); g.addPoint(new TPoint(186,212,0)); g.addPoint(new TPoint(183,211,0)); g.addPoint(new TPoint(177,208,0)); g.addPoint(new TPoint(169,206,0)); g.addPoint(new TPoint(162,205,0)); g.addPoint(new TPoint(154,207,0)); g.addPoint(new TPoint(145,209,0)); g.addPoint(new TPoint(137,210,0)); g.addPoint(new TPoint(129,214,0)); g.addPoint(new TPoint(122,217,0)); g.addPoint(new TPoint(118,218,0)); g.addPoint(new TPoint(111,221,0)); g.addPoint(new TPoint(109,222,0)); g.addPoint(new TPoint(110,219,0)); g.addPoint(new TPoint(112,217,0)); g.addPoint(new TPoint(118,209,0)); g.addPoint(new TPoint(120,207,0)); g.addPoint(new TPoint(128,196,0)); g.addPoint(new TPoint(135,187,0)); g.addPoint(new TPoint(138,183,0)); g.addPoint(new TPoint(148,167,0)); g.addPoint(new TPoint(157,153,0)); g.addPoint(new TPoint(163,145,0)); g.addPoint(new TPoint(165,142,0)); g.addPoint(new TPoint(172,133,0)); g.addPoint(new TPoint(177,127,0)); g.addPoint(new TPoint(179,127,0)); g.addPoint(new TPoint(180,125,0));
		recognizer.addTemplate("delete", g);
		
		g=new Gesture();
		g.addPoint(new TPoint(150,116,0)); g.addPoint(new TPoint(147,117,0)); g.addPoint(new TPoint(145,116,0)); g.addPoint(new TPoint(142,116,0)); g.addPoint(new TPoint(139,117,0)); g.addPoint(new TPoint(136,117,0)); g.addPoint(new TPoint(133,118,0)); g.addPoint(new TPoint(129,121,0)); g.addPoint(new TPoint(126,122,0)); g.addPoint(new TPoint(123,123,0)); g.addPoint(new TPoint(120,125,0)); g.addPoint(new TPoint(118,127,0)); g.addPoint(new TPoint(115,128,0)); g.addPoint(new TPoint(113,129,0)); g.addPoint(new TPoint(112,131,0)); g.addPoint(new TPoint(113,134,0)); g.addPoint(new TPoint(115,134,0)); g.addPoint(new TPoint(117,135,0)); g.addPoint(new TPoint(120,135,0)); g.addPoint(new TPoint(123,137,0)); g.addPoint(new TPoint(126,138,0)); g.addPoint(new TPoint(129,140,0)); g.addPoint(new TPoint(135,143,0)); g.addPoint(new TPoint(137,144,0)); g.addPoint(new TPoint(139,147,0)); g.addPoint(new TPoint(141,149,0)); g.addPoint(new TPoint(140,152,0)); g.addPoint(new TPoint(139,155,0)); g.addPoint(new TPoint(134,159,0)); g.addPoint(new TPoint(131,161,0)); g.addPoint(new TPoint(124,166,0)); g.addPoint(new TPoint(121,166,0)); g.addPoint(new TPoint(117,166,0)); g.addPoint(new TPoint(114,167,0)); g.addPoint(new TPoint(112,166,0)); g.addPoint(new TPoint(114,164,0)); g.addPoint(new TPoint(116,163,0)); g.addPoint(new TPoint(118,163,0)); g.addPoint(new TPoint(120,162,0)); g.addPoint(new TPoint(122,163,0)); g.addPoint(new TPoint(125,164,0)); g.addPoint(new TPoint(127,165,0)); g.addPoint(new TPoint(129,166,0)); g.addPoint(new TPoint(130,168,0)); g.addPoint(new TPoint(129,171,0)); g.addPoint(new TPoint(127,175,0)); g.addPoint(new TPoint(125,179,0)); g.addPoint(new TPoint(123,184,0)); g.addPoint(new TPoint(121,190,0)); g.addPoint(new TPoint(120,194,0)); g.addPoint(new TPoint(119,199,0)); g.addPoint(new TPoint(120,202,0)); g.addPoint(new TPoint(123,207,0)); g.addPoint(new TPoint(127,211,0)); g.addPoint(new TPoint(133,215,0)); g.addPoint(new TPoint(142,219,0)); g.addPoint(new TPoint(148,220,0)); g.addPoint(new TPoint(151,221,0));
		recognizer.addTemplate("left curly brace", g);
		
		g=new Gesture();
		g.addPoint(new TPoint(117,132,0)); g.addPoint(new TPoint(115,132,0)); g.addPoint(new TPoint(115,129,0)); g.addPoint(new TPoint(117,129,0)); g.addPoint(new TPoint(119,128,0)); g.addPoint(new TPoint(122,127,0)); g.addPoint(new TPoint(125,127,0)); g.addPoint(new TPoint(127,127,0)); g.addPoint(new TPoint(130,127,0)); g.addPoint(new TPoint(133,129,0)); g.addPoint(new TPoint(136,129,0)); g.addPoint(new TPoint(138,130,0)); g.addPoint(new TPoint(140,131,0)); g.addPoint(new TPoint(143,134,0)); g.addPoint(new TPoint(144,136,0)); g.addPoint(new TPoint(145,139,0)); g.addPoint(new TPoint(145,142,0)); g.addPoint(new TPoint(145,145,0)); g.addPoint(new TPoint(145,147,0)); g.addPoint(new TPoint(145,149,0)); g.addPoint(new TPoint(144,152,0)); g.addPoint(new TPoint(142,157,0)); g.addPoint(new TPoint(141,160,0)); g.addPoint(new TPoint(139,163,0)); g.addPoint(new TPoint(137,166,0)); g.addPoint(new TPoint(135,167,0)); g.addPoint(new TPoint(133,169,0)); g.addPoint(new TPoint(131,172,0)); g.addPoint(new TPoint(128,173,0)); g.addPoint(new TPoint(126,176,0)); g.addPoint(new TPoint(125,178,0)); g.addPoint(new TPoint(125,180,0)); g.addPoint(new TPoint(125,182,0)); g.addPoint(new TPoint(126,184,0)); g.addPoint(new TPoint(128,187,0)); g.addPoint(new TPoint(130,187,0)); g.addPoint(new TPoint(132,188,0)); g.addPoint(new TPoint(135,189,0)); g.addPoint(new TPoint(140,189,0)); g.addPoint(new TPoint(145,189,0)); g.addPoint(new TPoint(150,187,0)); g.addPoint(new TPoint(155,186,0)); g.addPoint(new TPoint(157,185,0)); g.addPoint(new TPoint(159,184,0)); g.addPoint(new TPoint(156,185,0)); g.addPoint(new TPoint(154,185,0)); g.addPoint(new TPoint(149,185,0)); g.addPoint(new TPoint(145,187,0)); g.addPoint(new TPoint(141,188,0)); g.addPoint(new TPoint(136,191,0)); g.addPoint(new TPoint(134,191,0)); g.addPoint(new TPoint(131,192,0)); g.addPoint(new TPoint(129,193,0)); g.addPoint(new TPoint(129,195,0)); g.addPoint(new TPoint(129,197,0)); g.addPoint(new TPoint(131,200,0)); g.addPoint(new TPoint(133,202,0)); g.addPoint(new TPoint(136,206,0)); g.addPoint(new TPoint(139,211,0)); g.addPoint(new TPoint(142,215,0)); g.addPoint(new TPoint(145,220,0)); g.addPoint(new TPoint(147,225,0)); g.addPoint(new TPoint(148,231,0)); g.addPoint(new TPoint(147,239,0)); g.addPoint(new TPoint(144,244,0)); g.addPoint(new TPoint(139,248,0)); g.addPoint(new TPoint(134,250,0)); g.addPoint(new TPoint(126,253,0)); g.addPoint(new TPoint(119,253,0)); g.addPoint(new TPoint(115,253,0));
		recognizer.addTemplate("right curly brace", g);
		
		g=new Gesture();
		g.addPoint(new TPoint(75,250,0)); g.addPoint(new TPoint(75,247,0)); g.addPoint(new TPoint(77,244,0)); g.addPoint(new TPoint(78,242,0)); g.addPoint(new TPoint(79,239,0)); g.addPoint(new TPoint(80,237,0)); g.addPoint(new TPoint(82,234,0)); g.addPoint(new TPoint(82,232,0)); g.addPoint(new TPoint(84,229,0)); g.addPoint(new TPoint(85,225,0)); g.addPoint(new TPoint(87,222,0)); g.addPoint(new TPoint(88,219,0)); g.addPoint(new TPoint(89,216,0)); g.addPoint(new TPoint(91,212,0)); g.addPoint(new TPoint(92,208,0)); g.addPoint(new TPoint(94,204,0)); g.addPoint(new TPoint(95,201,0)); g.addPoint(new TPoint(96,196,0)); g.addPoint(new TPoint(97,194,0)); g.addPoint(new TPoint(98,191,0)); g.addPoint(new TPoint(100,185,0)); g.addPoint(new TPoint(102,178,0)); g.addPoint(new TPoint(104,173,0)); g.addPoint(new TPoint(104,171,0)); g.addPoint(new TPoint(105,164,0)); g.addPoint(new TPoint(106,158,0)); g.addPoint(new TPoint(107,156,0)); g.addPoint(new TPoint(107,152,0)); g.addPoint(new TPoint(108,145,0)); g.addPoint(new TPoint(109,141,0)); g.addPoint(new TPoint(110,139,0)); g.addPoint(new TPoint(112,133,0)); g.addPoint(new TPoint(113,131,0)); g.addPoint(new TPoint(116,127,0)); g.addPoint(new TPoint(117,125,0)); g.addPoint(new TPoint(119,122,0)); g.addPoint(new TPoint(121,121,0)); g.addPoint(new TPoint(123,120,0)); g.addPoint(new TPoint(125,122,0)); g.addPoint(new TPoint(125,125,0)); g.addPoint(new TPoint(127,130,0)); g.addPoint(new TPoint(128,133,0)); g.addPoint(new TPoint(131,143,0)); g.addPoint(new TPoint(136,153,0)); g.addPoint(new TPoint(140,163,0)); g.addPoint(new TPoint(144,172,0)); g.addPoint(new TPoint(145,175,0)); g.addPoint(new TPoint(151,189,0)); g.addPoint(new TPoint(156,201,0)); g.addPoint(new TPoint(161,213,0)); g.addPoint(new TPoint(166,225,0)); g.addPoint(new TPoint(169,233,0)); g.addPoint(new TPoint(171,236,0)); g.addPoint(new TPoint(174,243,0)); g.addPoint(new TPoint(177,247,0)); g.addPoint(new TPoint(178,249,0)); g.addPoint(new TPoint(179,251,0)); g.addPoint(new TPoint(180,253,0)); g.addPoint(new TPoint(180,255,0)); g.addPoint(new TPoint(179,257,0)); g.addPoint(new TPoint(177,257,0)); g.addPoint(new TPoint(174,255,0)); g.addPoint(new TPoint(169,250,0)); g.addPoint(new TPoint(164,247,0)); g.addPoint(new TPoint(160,245,0)); g.addPoint(new TPoint(149,238,0)); g.addPoint(new TPoint(138,230,0)); g.addPoint(new TPoint(127,221,0)); g.addPoint(new TPoint(124,220,0)); g.addPoint(new TPoint(112,212,0)); g.addPoint(new TPoint(110,210,0)); g.addPoint(new TPoint(96,201,0)); g.addPoint(new TPoint(84,195,0)); g.addPoint(new TPoint(74,190,0)); g.addPoint(new TPoint(64,182,0)); g.addPoint(new TPoint(55,175,0)); g.addPoint(new TPoint(51,172,0)); g.addPoint(new TPoint(49,170,0)); g.addPoint(new TPoint(51,169,0)); g.addPoint(new TPoint(56,169,0)); g.addPoint(new TPoint(66,169,0)); g.addPoint(new TPoint(78,168,0)); g.addPoint(new TPoint(92,166,0)); g.addPoint(new TPoint(107,164,0)); g.addPoint(new TPoint(123,161,0)); g.addPoint(new TPoint(140,162,0)); g.addPoint(new TPoint(156,162,0)); g.addPoint(new TPoint(171,160,0)); g.addPoint(new TPoint(173,160,0)); g.addPoint(new TPoint(186,160,0)); g.addPoint(new TPoint(195,160,0)); g.addPoint(new TPoint(198,161,0)); g.addPoint(new TPoint(203,163,0)); g.addPoint(new TPoint(208,163,0)); g.addPoint(new TPoint(206,164,0)); g.addPoint(new TPoint(200,167,0)); g.addPoint(new TPoint(187,172,0)); g.addPoint(new TPoint(174,179,0)); g.addPoint(new TPoint(172,181,0)); g.addPoint(new TPoint(153,192,0)); g.addPoint(new TPoint(137,201,0)); g.addPoint(new TPoint(123,211,0)); g.addPoint(new TPoint(112,220,0)); g.addPoint(new TPoint(99,229,0)); g.addPoint(new TPoint(90,237,0)); g.addPoint(new TPoint(80,244,0)); g.addPoint(new TPoint(73,250,0)); g.addPoint(new TPoint(69,254,0)); g.addPoint(new TPoint(69,252,0));
		recognizer.addTemplate("star", g);
		
		g=new Gesture();
		g.addPoint(new TPoint(81,219,0)); g.addPoint(new TPoint(84,218,0)); g.addPoint(new TPoint(86,220,0)); g.addPoint(new TPoint(88,220,0)); g.addPoint(new TPoint(90,220,0)); g.addPoint(new TPoint(92,219,0)); g.addPoint(new TPoint(95,220,0)); g.addPoint(new TPoint(97,219,0)); g.addPoint(new TPoint(99,220,0)); g.addPoint(new TPoint(102,218,0)); g.addPoint(new TPoint(105,217,0)); g.addPoint(new TPoint(107,216,0)); g.addPoint(new TPoint(110,216,0)); g.addPoint(new TPoint(113,214,0)); g.addPoint(new TPoint(116,212,0)); g.addPoint(new TPoint(118,210,0)); g.addPoint(new TPoint(121,208,0)); g.addPoint(new TPoint(124,205,0)); g.addPoint(new TPoint(126,202,0)); g.addPoint(new TPoint(129,199,0)); g.addPoint(new TPoint(132,196,0)); g.addPoint(new TPoint(136,191,0)); g.addPoint(new TPoint(139,187,0)); g.addPoint(new TPoint(142,182,0)); g.addPoint(new TPoint(144,179,0)); g.addPoint(new TPoint(146,174,0)); g.addPoint(new TPoint(148,170,0)); g.addPoint(new TPoint(149,168,0)); g.addPoint(new TPoint(151,162,0)); g.addPoint(new TPoint(152,160,0)); g.addPoint(new TPoint(152,157,0)); g.addPoint(new TPoint(152,155,0)); g.addPoint(new TPoint(152,151,0)); g.addPoint(new TPoint(152,149,0)); g.addPoint(new TPoint(152,146,0)); g.addPoint(new TPoint(149,142,0)); g.addPoint(new TPoint(148,139,0)); g.addPoint(new TPoint(145,137,0)); g.addPoint(new TPoint(141,135,0)); g.addPoint(new TPoint(139,135,0)); g.addPoint(new TPoint(134,136,0)); g.addPoint(new TPoint(130,140,0)); g.addPoint(new TPoint(128,142,0)); g.addPoint(new TPoint(126,145,0)); g.addPoint(new TPoint(122,150,0)); g.addPoint(new TPoint(119,158,0)); g.addPoint(new TPoint(117,163,0)); g.addPoint(new TPoint(115,170,0)); g.addPoint(new TPoint(114,175,0)); g.addPoint(new TPoint(117,184,0)); g.addPoint(new TPoint(120,190,0)); g.addPoint(new TPoint(125,199,0)); g.addPoint(new TPoint(129,203,0)); g.addPoint(new TPoint(133,208,0)); g.addPoint(new TPoint(138,213,0)); g.addPoint(new TPoint(145,215,0)); g.addPoint(new TPoint(155,218,0)); g.addPoint(new TPoint(164,219,0)); g.addPoint(new TPoint(166,219,0)); g.addPoint(new TPoint(177,219,0)); g.addPoint(new TPoint(182,218,0)); g.addPoint(new TPoint(192,216,0)); g.addPoint(new TPoint(196,213,0)); g.addPoint(new TPoint(199,212,0)); g.addPoint(new TPoint(201,211,0));
		recognizer.addTemplate("pigtail", g);
	}

	
}
