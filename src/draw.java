import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MenuItem;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Line2D.Double;
import java.awt.geom.Rectangle2D;
import java.util.*;

import javax.swing.*;

class draw extends JFrame
{
	static draw frame;
	ArrayList<Rectangle> rectangles;
	ArrayList<Circle> circles;
	ArrayList<Polygon> polygons;
	ArrayList<Shape>shapes;
	JComboBox<String> shape_list;
	int type=0,mode=0,pcount=0,rcount=0,ccount=0;
	cord s0=null,s1=null,s2=null;
	int stat;
	int snap=0;
	Shape drawing,editing,bad;
	boolean renew=false,any_select=false;
	ArrayList<Shape>selected;
	JLabel show_color;
	JSlider R,G,B;
	
	draw()
	{
		add(new tool());
		JMenuBar menu=new JMenuBar();
		setJMenuBar(menu);
		
		selected=new ArrayList<Shape>();
		
		JRadioButtonMenuItem circ=new JRadioButtonMenuItem("Circle");
		JRadioButtonMenuItem rect=new JRadioButtonMenuItem("Rectangle");
		JRadioButtonMenuItem poly=new JRadioButtonMenuItem("Polygon");
		
		JMenu shape_type=new JMenu("Type");
		shape_type.add(circ);
		shape_type.add(rect);
		shape_type.add(poly);
		
		shape_list=new JComboBox<>();
		
		shape_list.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e) {
				if (!selected.isEmpty())
				{
					selected.removeAll(selected);
					for (Shape s:shapes)
						if (s.name.equals(shape_list.getSelectedItem()))
						{
							s.selected=true;
							selected.add(s);
						}
				}
				repaint();
			}
		}
	);
		
		circ.setSelected(true);
		
		circ.addItemListener(new ItemListener()
				{
					public void itemStateChanged(ItemEvent e) {
						if (circ.isSelected()) type=0;
					}
				}
			);
		
		rect.addItemListener(new ItemListener()
			{
				public void itemStateChanged(ItemEvent e) {
					if (rect.isSelected()) type=1;
				}
			}
		);
		
		poly.addItemListener(new ItemListener()
			{
				public void itemStateChanged(ItemEvent e) {
					if (poly.isSelected()) type=2;
				}
			}
		);
			
		JButton del=new JButton("Delete");
		
		del.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) {
				for (int i=0;i<rectangles.size();i++)
					if (rectangles.get(i).name.equals(shape_list.getSelectedItem()))
							rectangles.remove(i);
					
				for (int i=0;i<circles.size();i++)
					if (circles.get(i).name.equals(shape_list.getSelectedItem()))
							circles.remove(i);
				
				for (int i=0;i<polygons.size();i++)
					if (polygons.get(i).name.equals(shape_list.getSelectedItem()))
							polygons.remove(i);
				
				shape_list.removeItem(shape_list.getSelectedItem());
				repaint();
			}
		}
		);
		
		JButton ref=new JButton("Refresh");
		
		ref.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e) {
						rectangles.removeAll(rectangles);
						circles.removeAll(circles);
						polygons.removeAll(polygons);
						shape_list.removeAllItems();
						repaint();
						stat=0;
						
					}});
		
		JSlider scalex=new JSlider(SwingConstants.HORIZONTAL,0,200,0);
		scalex.setMajorTickSpacing(20);
		scalex.setMinorTickSpacing(20);
		scalex.setPaintTicks(true);
		scalex.setValue(100);
		
		JSlider scaley=new JSlider(SwingConstants.HORIZONTAL,0,200,0);
		scaley.setMajorTickSpacing(20);
		scaley.setMinorTickSpacing(20);
		scaley.setPaintTicks(true);
		scaley.setValue(100);
		
		JCheckBox fix=new JCheckBox("Fix");
		fix.setSelected(true);
		
		JTextField sx_text=new JTextField("100%");
		JTextField sy_text=new JTextField("100%");
		
		scalex.addMouseMotionListener(new MouseMotionListener()
				{
					public void mouseDragged(MouseEvent e) {
						sx_text.setText(String.valueOf(scalex.getValue())+'%');
						if (stat==3)
						{
							for (Shape s:selected)
							{
								s.scalex(scalex.getValue());
								if (fix.isSelected())
								{
									sy_text.setText(String.valueOf(scalex.getValue())+'%');
									scaley.setValue(scalex.getValue());
									s.scaley(scaley.getValue());
								}												
							}
							repaint();
						}

					}
					public void mouseMoved(MouseEvent e) {

					}
				});
				
		scalex.addMouseListener(new MouseListener()
			{
				public void mouseClicked(MouseEvent e)
				{
				}
				
				public void mousePressed(MouseEvent e) {
		
					if (e.getButton()==MouseEvent.BUTTON1)
						snap++;
					if (snap==2) scalex.setSnapToTicks(true);
					sx_text.setText(String.valueOf(scalex.getValue())+'%');
					if (!renew) 
					{
						for (Shape s:shapes)
							s.prescale();
						renew=true;
					}
					if (fix.isSelected()) 
					{
						sy_text.setText(String.valueOf(scalex.getValue())+'%');
						scaley.setValue(scalex.getValue());
					}
			
				}

				public void mouseReleased(MouseEvent e) {
					snap--;
					scalex.setSnapToTicks(false);
					sx_text.setText(String.valueOf(scalex.getValue())+'%');
					if (fix.isSelected())
					{
						sy_text.setText(String.valueOf(scalex.getValue())+'%');
						scaley.setValue(scalex.getValue());
					}
					
				}

				public void mouseEntered(MouseEvent e) {
			
				}

				public void mouseExited(MouseEvent e) {
					
				}
			});
		
		scalex.addKeyListener(new KeyListener()
			{
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_SHIFT) snap++;
					if (snap==2)
						scalex.setSnapToTicks(true);
				}
				
				public void keyReleased(KeyEvent e) {
					snap--;
					scalex.setSnapToTicks(false);
				}
	
				public void keyTyped(KeyEvent e) {
							}

			});
		
		scaley.addMouseMotionListener(new MouseMotionListener()
		{
			public void mouseDragged(MouseEvent e) {
				sy_text.setText(String.valueOf(scaley.getValue())+'%');
				if (stat==3)
				{
					for (Shape s:selected)
					{
						s.scaley(scaley.getValue());
						if (fix.isSelected())
						{
							sx_text.setText(String.valueOf(scaley.getValue())+'%');
							scalex.setValue(scaley.getValue());
							s.scalex(scalex.getValue());
						}
					}
					repaint();
				}
			}
			public void mouseMoved(MouseEvent e) {

			}
		});
		
	scaley.addMouseListener(new MouseListener()
		{
			public void mouseClicked(MouseEvent e)
			{
			}
			
			public void mousePressed(MouseEvent e) {
				if (!renew) 
				{
					for (Shape s:selected)
						s.prescale();
					renew=true;
				}
				if (e.getButton()==MouseEvent.BUTTON1)
					snap++;
				if (snap==2) scaley.setSnapToTicks(true);
				sy_text.setText(String.valueOf(scaley.getValue())+'%');
				if (fix.isSelected()) 
				{
					sx_text.setText(String.valueOf(scaley.getValue())+'%');
					scalex.setValue(scaley.getValue());
				}
			}
	
			public void mouseReleased(MouseEvent e) {
				snap--;
				scaley.setSnapToTicks(false);
				sy_text.setText(String.valueOf(scaley.getValue())+'%');
				if (fix.isSelected())
				{
					sx_text.setText(String.valueOf(scaley.getValue())+'%');
					scalex.setValue(scaley.getValue());
				}
			}
	
			public void mouseEntered(MouseEvent e) {	
			}
	
			public void mouseExited(MouseEvent e) {			
			}
		});
	
	scaley.addKeyListener(new KeyListener()
		{
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_SHIFT) snap++;
				if (snap==2)
					scaley.setSnapToTicks(true);
			}
			
			public void keyReleased(KeyEvent e) {
				snap--;
				scaley.setSnapToTicks(false);
			}
	
			public void keyTyped(KeyEvent e) {
						}
	
		});

		JMenu scales=new JMenu("Scale");
		scales.add(scalex);
		scales.add(sx_text);
		scales.add(scaley);
		scales.add(sy_text);
		scales.add(fix);
		
		scales.addMouseListener(new MouseListener()
		{

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				scalex.setValue(100);
				scaley.setValue(100);
				sx_text.setText("100%");
				sy_text.setText("100%");
				renew=false;
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
	
		});

		
		JSlider deg=new JSlider(SwingConstants.HORIZONTAL,0,360,0);
		deg.setMajorTickSpacing(45);
		deg.setMinorTickSpacing(15);
		deg.setPaintTicks(true);
		deg.setValue(0);
		
		JTextField deg_text=new JTextField("0");

		deg.addMouseMotionListener(new MouseMotionListener()
						{
							public void mouseDragged(MouseEvent e) {
								deg_text.setText(String.valueOf(deg.getValue()));
								if (stat==3)
								{
									for (Shape s:selected)
										s.deg=(s.deg0+deg.getValue())%360;										
									repaint();
								}

							}
							public void mouseMoved(MouseEvent e) {

							}
						});
						
				deg.addMouseListener(new MouseListener()
					{
						public void mouseClicked(MouseEvent e)
						{
						}
						
						public void mousePressed(MouseEvent e) {
				
							if (e.getButton()==MouseEvent.BUTTON1)
								snap++;
							if (!renew) 
							{
								for (Shape s:shapes)
									s.prerotate();
								renew=true;
							}
							if (snap==2) deg.setSnapToTicks(true);
							deg_text.setText(String.valueOf(deg.getValue()));
					
						}

						public void mouseReleased(MouseEvent e) {
							snap--;
							deg.setSnapToTicks(false);
							deg_text.setText(String.valueOf(deg.getValue()));
							
						}

						public void mouseEntered(MouseEvent e) {
					
						}

						public void mouseExited(MouseEvent e) {
							
						}
					});
				
				deg.addKeyListener(new KeyListener()
					{
						public void keyPressed(KeyEvent e) {
							if(e.getKeyCode() == KeyEvent.VK_SHIFT) snap++;
							if (snap==2)
								deg.setSnapToTicks(true);
						}
						
						public void keyReleased(KeyEvent e) {
							snap--;
							deg.setSnapToTicks(false);
						}
			
						public void keyTyped(KeyEvent e) {
									}

					});
				

				
		JMenu rotate=new JMenu("Rotate");
		rotate.add(deg);
		rotate.add(deg_text);
		
		R=new JSlider(SwingConstants.HORIZONTAL,0,255,0);
		R.setMajorTickSpacing(32);
		R.setMinorTickSpacing(32);
		R.setPaintTicks(true);
		R.setValue(0);
		R.setToolTipText("R");
		
		G=new JSlider(SwingConstants.HORIZONTAL,0,255,0);
		G.setMajorTickSpacing(32);
		G.setMinorTickSpacing(32);
		G.setPaintTicks(true);
		G.setValue(0);
		G.setToolTipText("G");
		
		B=new JSlider(SwingConstants.HORIZONTAL,0,255,0);
		B.setMajorTickSpacing(32);
		B.setMinorTickSpacing(32);
		B.setPaintTicks(true);
		B.setValue(0);
		B.setToolTipText("B");
		
		R.addMouseMotionListener(new ColorHandler());
		G.addMouseMotionListener(new ColorHandler());
		B.addMouseMotionListener(new ColorHandler());
		
		show_color=new JLabel("¡ö");
		show_color.setForeground(new Color(R.getValue(),G.getValue(),B.getValue()));
		
		JMenu color=new JMenu("Color");
		color.add(R);
		color.add(G);
		color.add(B);
		color.add(show_color);
		
		ButtonGroup group=new ButtonGroup();
		
		group.add(circ);
		group.add(rect);
		group.add(poly);

		menu.add(shape_type);
		menu.add(scales);
		menu.add(rotate);
		menu.add(color);
		menu.add(del);
		menu.add(ref);
		menu.add(shape_list);
		
		
		bad=new Shape();
		drawing=bad;
		editing=bad;
		
	 }
	
	class ColorHandler implements  MouseMotionListener
	{
		public void mouseDragged(MouseEvent e) {
			show_color.setForeground(new Color(R.getValue(),G.getValue(),B.getValue()));
			for (Shape s:selected)
				s.color=new Color(R.getValue(),G.getValue(),B.getValue());

			if (drawing.type!=-1)
				drawing.color=new Color(R.getValue(),G.getValue(),B.getValue());
			repaint();
		}
		public void mouseMoved(MouseEvent e) {
		}
	}

	class tool extends JComponent
	{
		cord last=null;
		tool()
		{
			stat=0;
			rectangles=new ArrayList<Rectangle>();
			circles=new ArrayList<Circle>();
			polygons=new ArrayList<Polygon>();
			shapes=new ArrayList<Shape>();
			addMouseListener(new MouseHandler());
			addMouseMotionListener(new MouseMotionHandler());
		}
		
		double max(double a,double b)
		{
			if (a>b) return a; else return b;
		}
		
		double min(double a,double b)
		{
			if (a<b) return a; else return b;
		}
		
		public void paintComponent(Graphics g)
		{
			Graphics2D g2=(Graphics2D) g;
			cord c1,c2;
			Stroke st=g2.getStroke(),sp=new BasicStroke((float) 1.5);
			for (Rectangle s:rectangles)
			{
					if (selected.contains(s)) 
						g2.setStroke(sp);
					else 
						g2.setStroke(st);
					g2.setColor(s.color);
					AffineTransform trans = new AffineTransform();
					if (s.deg!=0) trans.rotate(s.deg*Math.PI/180, s.centre.x, s.centre.y);
					g2.setTransform(trans);
					g2.draw(new Rectangle2D.Double(s.left,s.up,s.right-s.left,s.down-s.up));
				
			}

			for (Polygon s:polygons)
			{
				if (selected.contains(s)) 
					g2.setStroke(sp);
				else 
					g2.setStroke(st);
				g2.setColor(s.color);
				
				AffineTransform trans = new AffineTransform();
				if (s.deg!=0) trans.rotate(s.deg*Math.PI/180, s.centre.x, s.centre.y);
				g2.setTransform(trans);
				
				for (int i=0;i<s.cords.size()-1;i++)
				{
					c1=s.cords.get(i);
					c2=s.cords.get(i+1);
					g2.draw(new Line2D.Double(c1.x,c1.y,c2.x,c2.y));
				}
				
				if (s.current&&last!=null&!s.cords.isEmpty())
				{
					c1=s.cords.get(s.cords.size()-1);
					g2.draw(new Line2D.Double(c1.x,c1.y,last.x,last.y));
				}
			
				if (s.cycle&&s.cords.size()>2&&!s.current)
				{
					c1=s.cords.get(s.cords.size()-1);
					c2=s.cords.get(0);
					g2.draw(new Line2D.Double(c1.x,c1.y,c2.x,c2.y));
				}
			}
			
			for (Circle s:circles)
			{
				if (selected.contains(s)) 
					g2.setStroke(sp);
				else 
					g2.setStroke(st);
				g2.setColor(s.color);
				
				AffineTransform trans = new AffineTransform();
				if (s.deg!=0) trans.rotate(s.deg*Math.PI/180, s.centre.x, s.centre.y);
				g2.setTransform(trans);
				
				g2.draw(new Ellipse2D.Double(s.centre.x-s.ra,s.centre.y-s.rb,2*s.ra,2*s.rb));
			}
			
			g2.setTransform(new AffineTransform());
			g2.setColor(Color.black);
			if (s0!=null&&s1!=null)
			{
				g2.setStroke(new BasicStroke(1,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,0,new float[]{5,5},0));
				double up,left;
				if (s0.x<s1.x) left=s0.x; else left=s1.x; 
				if (s0.y<s1.y) up=s0.y; else up=s1.y;
				g2.draw(new Rectangle2D.Double(left,up,Math.abs(s1.x-s0.x),Math.abs(s1.y-s0.y)));
				g2.setStroke(st);
			}
		}
		
		class MouseHandler implements MouseListener
		{
			public void mouseClicked(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				if (stat==0)
				{
					frame.setCursor(CROSSHAIR_CURSOR);
					s1=null;
					s0=new cord(e.getX(),e.getY());
					System.out.print("Press:");
					System.out.println(stat);
				}
				else if (stat==3)
					s2=new cord(e.getX(),e.getY());
			}
			
			public void mouseReleased(MouseEvent e) {
	
				int mod=e.getButton();
				if (mod==MouseEvent.BUTTON1)
				{
						switch(type)
						{
							case 0:
								if (stat==0)
								{
									stat=1;
									drawing=new Circle(new cord(e.getX(),e.getY()),new Color(R.getValue(),G.getValue(),B.getValue()),ccount++);
									shape_list.addItem(drawing.name);
									shape_list.setSelectedIndex(shape_list.getItemCount()-1);
									drawing.selected=true;
									for (Shape s:selected)
										s.selected=false;
									selected.removeAll(selected);
									selected.add(drawing);
									circles.add((Circle) drawing);
								}
								else if (stat==1)
								{
									circles.get(circles.size()-1).add(new cord(e.getX(),e.getY()));
									shapes.add(drawing);
									drawing=bad;
									frame.setCursor(DEFAULT_CURSOR);
									stat=0;
								}
	
							
								break;
							case 1:
								if (stat==0)
								{
									stat=1;
									drawing=new Rectangle(new cord(e.getX(),e.getY()),new Color(R.getValue(),G.getValue(),B.getValue()),rcount++);
									shape_list.addItem(drawing.name);
									shape_list.setSelectedIndex(shape_list.getItemCount()-1);
									drawing.selected=true;
									for (Shape s:selected)
										s.selected=false;
									selected.removeAll(selected);
									selected.add(drawing);
									rectangles.add((Rectangle) drawing);
									repaint();
								}
								else if (stat==1)
								{
									rectangles.get(rectangles.size()-1).add(new cord(e.getX(),e.getY()));
									shapes.add(drawing);
									frame.setCursor(DEFAULT_CURSOR);
									stat=0;
									drawing=bad;
								}
								break;
							case 2:
								if (stat==0)
								{
									stat=1;
									drawing=new Polygon(new cord(e.getX(),e.getY()),new Color(R.getValue(),G.getValue(),B.getValue()),pcount++);
									shape_list.addItem(drawing.name);
									shape_list.setSelectedIndex(shape_list.getItemCount()-1);
									drawing.selected=true;
									for (Shape s:selected)
										s.selected=false;
									selected.removeAll(selected);
									selected.add(drawing);
									polygons.add((Polygon)drawing);
									repaint();
								}
								else if (stat==1)
								{
									polygons.get(polygons.size()-1).add(new cord(e.getX(),e.getY()));
									last=null;
									
								}
								break;
						}
						
						if (stat==2) 
						{
							stat=0;
							double up,down,left,right;
							left=min(s0.x,s1.x);
							right=max(s0.x,s1.x);
							up=min(s0.y,s1.y);
							down=max(s0.y,s1.y);
							selected.removeAll(selected);
							for (Shape s:shapes)
							{
								if (s.up>=up&&s.down<=down&&s.left>=left&&s.right<=right)
								{
									selected.add(s);
									s.selected=true;
								}
							}
							if (selected.isEmpty())
							{
								for (Shape s:shapes)
									if (shape_list.getSelectedItem().equals(s.name))
									{
										selected.add(s);
										s.selected=true;
									}
							}
						}
						repaint();
					}
					else if (mod==MouseEvent.BUTTON3)
					{
						for (Shape s:selected)
							s.selected=false;
						selected.removeAll(selected);
						for (Shape s:shapes)
						{
							if (shape_list.getSelectedItem().equals(s.name))
							{
								selected.add(s);
								s.selected=true;
							}
						}				
						stat=0;
						if (stat==3)
						{
							s1=null;						
							repaint();
							frame.setCursor(DEFAULT_CURSOR);
							return;
						}
						last=null;
						drawing.current=false;
						drawing.cycle=false;
						if (drawing.type!=-1&&!drawing.ready) 
						{
							switch(type)
							{
							case 0:
								circles.remove(drawing);
							case 1:
								rectangles.remove(drawing);
							case 2:
								polygons.remove(drawing);
							}
							shape_list.removeItemAt(shape_list.getSelectedIndex());
						}
						else if (type==2)
							shapes.add(drawing);
						frame.setCursor(DEFAULT_CURSOR);
						drawing=bad;
						repaint();
					}
					else
					{
						if (stat==0)
						{
							s1=null;
							for (Shape s:selected)
							{
									R.setValue(s.color.getRed());
									G.setValue(s.color.getGreen());
									B.setValue(s.color.getBlue());
									show_color.setForeground(s.color);	
									if (s1==null)
									{
										s0=new cord(s.left,s.up);
										s1=new cord(s.right,s.down);
									}
									else
									{
										s0.set(min(s0.x,s.left),min(s0.y,s.up));
										s1.set(max(s1.x,s.right),max(s1.y,s.down));
									}
							}
							repaint();
							stat=3;
							frame.setCursor(MOVE_CURSOR);
 						}
						else if (stat==1&&drawing.type==2)
						{
							stat=0;
							last=null;
							drawing.current=false;
							drawing.cycle=true;
							frame.setCursor(DEFAULT_CURSOR);
							shapes.add(drawing);
							drawing=bad;
							repaint();
						}
							
					}
				
				System.out.print("Release:");
				System.out.println(stat);			
			}

			
			public void mouseEntered(MouseEvent e) {

				
			}

			
			public void mouseExited(MouseEvent e) {

				
			}
			
		}
		
		class MouseMotionHandler implements MouseMotionListener
		{

			
			public void mouseDragged(MouseEvent e) {
				if (stat==0||stat==2)
				{
					frame.setCursor(DEFAULT_CURSOR);
					stat=2;
					s1=new cord(e.getX(),e.getY());
					repaint();
				}
				else if (stat==3)
				{
					s2.x=e.getX()-s2.x;
					s2.y=e.getY()-s2.y;
					s0.move(s2);
					s1.move(s2);
					for (Shape s:selected)
						s.move(s2);
					s2.x=e.getX();
					s2.y=e.getY();
					repaint();
				}
				System.out.print("Drag:");
				System.out.println(stat);
			}

			
			public void mouseMoved(MouseEvent e) {
				
				if (stat==1)
				{
					switch(type)
					{
					case 0:
						drawing.add(new cord(e.getX(), e.getY()));
						repaint();
						break;
					case 1:
						drawing.add(new cord(e.getX(), e.getY()));
						repaint();
						break;
					case 2:
						last=new cord(e.getX(),e.getY());
						repaint();
					}
				}
				else if (stat==3)
				{
					
				}
	
			}
		}
		
	}
	
	public static void main(String []args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{			
				frame=new draw();
				//frame.setLayout(new FlowLayout());
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);
				frame.setSize(500,500);
				frame.setExtendedState(0);
				frame.toFront();
				frame.setResizable(false);
			}
		});
	}
}

class cord
{
	public double x,y;
	cord(double left,double up)
	{
		x=left;
		y=up;
	}
	cord(cord c)
	{
		x=c.x;
		y=c.y;
	}
	void move(cord c)
	{
		x+=c.x;
		y+=c.y;
	}
	void set(double d,double e)
	{
		x=d;
		y=e;
	}
}

class Shape
{
	int type;
	double up,down,left,right,u0,d0,l0,r0;
	String name;
	ArrayList<cord>cords,origin;
	cord last,centre;
	cord s0,s1,ps0,ps1;
	double ra,r1,rb,r2;
	Color color;
	boolean current,ready=false,cycle=false,selected=false,edited=false;
	double deg=0,deg0=0;
	Shape(){type=-1;}
	void add(cord c) {}
	void move(cord c) {}
	double max(double a,double b)
	{
		if (a>b) return a; else return b;
	}
	
	double min(double a,double b)
	{
		if (a<b) return a; else return b;
	}
	void scalex(double k) {
	}
	
	void scaley(double k) {

	}
	
	void prescale() {

	}
	
	void prerotate()
	{
		deg0=deg;
	}
}

class Circle extends Shape
{
	Ellipse2D show;
	Circle(cord c,Color clr,int count)
	{
		name="Circle"+String.valueOf(count);
		type=0;
		centre=c;
		color=clr;
		current=true;
		deg=0;
	}
	
	void add(cord c)
	{
		ra=Math.sqrt(Math.pow(c.x-centre.x,2)+Math.pow(c.y-centre.y,2));
		rb=ra;
		up=centre.y-rb;
		down=centre.y+rb;
		left=centre.x-ra;
		right=centre.x+ra;
	}
	
	void move(cord c)
	{
		centre.move(c);
		left+=c.x;
		right+=c.x;
		up+=c.y;
		down+=c.y;
	}
	
	void prescale()
	{
		u0=up;
		d0=down;
		l0=left;
		r0=right;
		r1=ra;
		r2=rb;
	}
	
	void scalex(double k)
	{
		ra=r1*k/100;
		left=centre.x+(l0-centre.x)*k/100;
		right=centre.x+(r0-centre.x)*k/100;
	}
	
	void scaley(double k)
	{
		rb=r2*k/100;
		up=centre.y+(u0-centre.y)*k/100;
		down=centre.y+(d0-centre.y)*k/100;
	}
}

class Rectangle extends Shape
{
	
	Rectangle(cord c,Color clr,int count)
	{
		name="Rectangle"+String.valueOf(count);
		type=1;
		s0=c;
		last=null;
		current=true;
		color=clr;
		up=c.y;
		down=c.y;
		left=c.x;
		right=c.x;
		centre=new cord(0,0);
		deg=0;
	}
	
	void add(cord c)
	{
		s1=c;
		left=min(s0.x,c.x);
		right=max(s0.x,c.x);
		up=min(s0.y,c.y);
		down=max(s0.y,c.y);
		centre.set((left+right)/2,(up+down)/2);
		System.out.print(centre.x);
		System.out.print(s0.x);
		System.out.println(s1.x);
	}
	
	void move(cord c)
	{
		s0.move(c);
		s1.move(c);
		left+=c.x;
		right+=c.x;
		up+=c.y;
		down+=c.y;
		centre.set((left+right)/2,(up+down)/2);
	}
	
	
	void prescale()
	{
		ps0=new cord(s0);
		ps1=new cord(s1);
		u0=up;
		d0=down;
		l0=left;
		r0=right;
	}
	
	void scaley(double k) 
	{
		s0.y=centre.y+(ps0.y-centre.y)*k/100;
		s1.y=centre.y+(ps1.y-centre.y)*k/100;
		up=s0.y;
		down=s1.y;
	}
	void scalex(double k)
	{
		s0.x=centre.x+(ps0.x-centre.x)*k/100;
		s1.x=centre.x+(ps1.x-centre.x)*k/100;
		left=s0.x;
		right=s1.x;
	}

}

class Polygon extends Shape
{
	Polygon(cord c,Color clr,int count)
	{
		name="Polygon"+String.valueOf(count);
		type=2;
		cords=new ArrayList<cord>();
		cords.add(c);
		origin=new ArrayList<cord>();
		last=null;
		current=true;
		color=clr;
		up=c.y;
		down=c.y;
		left=c.x;
		right=c.x;
		centre=new cord(0,0);
		deg=0;
	}
	
	void add(cord c)
	{
		cords.add(c);
		if (c.x<left) left=c.x;
		if (c.x>right) right=c.x;
		if (c.y<up) up=c.y;
		if (c.y>down) down=c.y;
		if (cords.size()>1) ready=true;
		centre.set((left+right)/2,(up+down)/2);
	}
	
	void move(cord c)
	{
		for (int i=0;i<cords.size();i++)
			cords.get(i).move(c);
		centre.move(c);
		left+=c.x;
		right+=c.x;
		up+=c.y;
		down+=c.y;
		
	}
	
	void prescale()
	{
		origin.removeAll(origin);
		for (cord c:cords)
			origin.add(new cord(c));
		u0=up;
		d0=down;
		l0=left;
		r0=right;
	}
	
	void scalex(double k)
	{
		for (int i=0;i<cords.size();i++)	
			cords.get(i).x=centre.x+(origin.get(i).x-centre.x)*k/100;
		left=centre.x+(l0-centre.x)*k/100;
		right=centre.x+(r0-centre.x)*k/100;	
	}
	
	void scaley(double k)
	{
		for (int i=0;i<cords.size();i++)
			cords.get(i).y=centre.y+(origin.get(i).y-centre.y)*k/100;
		up=centre.y+(u0-centre.y)*k/100;
		down=centre.y+(d0-centre.y)*k/100;	
	}
	
}

