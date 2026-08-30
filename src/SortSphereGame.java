import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import javax.imageio.ImageIO;

/** A self-contained Swing sorting visualizer. Requires only a JDK. */
public class SortSphereGame extends JFrame {
    private final JTextField numbers = new JTextField("8, 3, 10, 1, 6, 4", 30);
    private final JComboBox<String> algorithm = new JComboBox<>(new String[]{"Bubble sort", "Selection sort", "Insertion sort", "Merge sort"});
    private final BallBoard board = new BallBoard();
    private final JLabel status = new JLabel("Enter 2–12 whole numbers, then press Start.");
    private final JLabel progress = new JLabel("Progress: 0 / 4 algorithms mastered");
    private final Set<String> mastered = new LinkedHashSet<>();
    private java.util.List<Move> moves = Collections.emptyList();
    private int moveIndex, comparisons, swaps;
    private javax.swing.Timer timer;
    private JButton start, step, reset, certificate;

    public SortSphereGame() {
        super("SortSphere — Sorting Game");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 620));
        setLocationByPlatform(true);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBorder(new EmptyBorder(18, 22, 18, 22));
        root.setBackground(new Color(246, 249, 255));
        setContentPane(root);

        JPanel top = new JPanel(new BorderLayout(10, 8)); top.setOpaque(false);
        JLabel title = new JLabel("SortSphere"); title.setFont(new Font("SansSerif", Font.BOLD, 30)); title.setForeground(new Color(42, 66, 151));
        JLabel subtitle = new JLabel("Make the balls find their place — one comparison at a time."); subtitle.setForeground(new Color(85, 94, 120));
        JPanel heading = new JPanel(new GridLayout(2, 1)); heading.setOpaque(false); heading.add(title); heading.add(subtitle); top.add(heading, BorderLayout.NORTH);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); controls.setOpaque(false);
        controls.add(new JLabel("Numbers:")); controls.add(numbers); controls.add(new JLabel("Algorithm:")); controls.add(algorithm);
        start = new JButton("Start animation"); step = new JButton("Step"); reset = new JButton("Reset"); certificate = new JButton("Save certificate");
        controls.add(start); controls.add(step); controls.add(reset); controls.add(certificate); top.add(controls, BorderLayout.SOUTH);
        root.add(top, BorderLayout.NORTH);

        board.setBackground(Color.WHITE); root.add(board, BorderLayout.CENTER);
        JPanel footer = new JPanel(new GridLayout(2, 1, 0, 4)); footer.setOpaque(false);
        status.setFont(new Font("SansSerif", Font.PLAIN, 15)); progress.setFont(new Font("SansSerif", Font.BOLD, 14)); progress.setForeground(new Color(106, 78, 160));
        footer.add(status); footer.add(progress); root.add(footer, BorderLayout.SOUTH);
        start.addActionListener(e -> startRun()); step.addActionListener(e -> doStep()); reset.addActionListener(e -> resetRun()); certificate.addActionListener(e -> saveCertificate());
        resetRun();
    }

    private int[] parseNumbers() {
        String[] tokens = numbers.getText().trim().split("[\\s,]+ ".trim());
        if (tokens.length < 2 || tokens.length > 12) throw new IllegalArgumentException("Please enter between 2 and 12 whole numbers.");
        int[] a = new int[tokens.length];
        for (int i=0;i<a.length;i++) { a[i] = Integer.parseInt(tokens[i]); if (a[i] < -99 || a[i] > 999) throw new IllegalArgumentException("Use values from -99 to 999."); }
        return a;
    }

    private void resetRun() {
        stopTimer();
        try { board.setValues(parseNumbers()); status.setText("Pick an algorithm. Gold balls are being compared; coral balls are swapping."); }
        catch (Exception ex) { board.setValues(new int[]{8,3,10,1,6,4}); status.setText("Enter comma-separated whole numbers."); }
        moves = Collections.emptyList(); moveIndex=0; comparisons=swaps=0; setButtons(true);
    }
    private void startRun() {
        try {
            int[] a = parseNumbers(); String name = (String)algorithm.getSelectedItem();
            moves = makeMoves(a, name); moveIndex=0; comparisons=swaps=0; board.setValues(a); setButtons(false);
            status.setText(explanation(name) + " Watch what each comparison changes.");
            timer = new javax.swing.Timer(520, e -> doStep()); timer.start();
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Check your numbers", JOptionPane.WARNING_MESSAGE); }
    }
    private void doStep() {
        if (moves.isEmpty() || moveIndex >= moves.size()) { finishRun(); return; }
        Move m = moves.get(moveIndex++); if (m.compare) comparisons++; if (m.swap) swaps++;
        board.apply(m); status.setText(m.text + "   Comparisons: " + comparisons + " • Moves: " + swaps);
        if (moveIndex >= moves.size()) finishRun();
    }
    private void finishRun() {
        stopTimer(); if (moves.isEmpty()) return; setButtons(true);
        String name = (String)algorithm.getSelectedItem(); mastered.add(name); progress.setText("Progress: " + mastered.size() + " / 4 algorithms mastered  " + badgeText());
        status.setText("✓ " + name + " complete! " + explanation(name));
        if (mastered.size() == 4) JOptionPane.showMessageDialog(this, "Sorting Champion unlocked! Save your certificate.", "All badges earned", JOptionPane.INFORMATION_MESSAGE);
    }
    private void stopTimer() { if (timer != null) { timer.stop(); timer=null; } }
    private void setButtons(boolean ready) { start.setEnabled(ready); step.setEnabled(ready); reset.setEnabled(ready); }
    private String badgeText() { StringBuilder b=new StringBuilder(); for(String s:mastered) b.append("🏅 ").append(s.replace(" sort", "")).append("  "); return b.toString(); }
    private String explanation(String n) {
        if (n.startsWith("Bubble")) return "Bubble sort repeatedly swaps neighboring out-of-order balls; large values drift right.";
        if (n.startsWith("Selection")) return "Selection sort finds the smallest remaining ball, then places it at the front.";
        if (n.startsWith("Insertion")) return "Insertion sort grows a sorted left side, sliding each new ball into its spot.";
        return "Merge sort splits into small groups, then merges them back in sorted order.";
    }

    private java.util.List<Move> makeMoves(int[] source, String name) {
        int[] a=source.clone(); java.util.List<Move> out=new ArrayList<>();
        if(name.startsWith("Bubble")) for(int end=a.length-1;end>0;end--) for(int i=0;i<end;i++){ out.add(Move.compare(i,i+1,"Compare neighbors " + a[i] + " and " + a[i+1])); if(a[i]>a[i+1]){swap(a,i,i+1);out.add(Move.swap(i,i+1,"Swap: " + a[i+1] + " belongs before " + a[i]));} }
        else if(name.startsWith("Selection")) for(int i=0;i<a.length-1;i++){int min=i;for(int j=i+1;j<a.length;j++){out.add(Move.compare(min,j,"Look for the smallest: compare " + a[min] + " and " + a[j]));if(a[j]<a[min])min=j;}if(min!=i){swap(a,i,min);out.add(Move.swap(i,min,"Place the smallest remaining ball at position " + (i+1)));}}
        else if(name.startsWith("Insertion")) for(int i=1;i<a.length;i++){int j=i;while(j>0){out.add(Move.compare(j-1,j,"Compare the new ball with its left neighbor"));if(a[j-1]<=a[j])break;swap(a,j-1,j);out.add(Move.swap(j-1,j,"Slide the ball left into the sorted group"));j--;}}
        else { mergeMoves(a,0,a.length-1,out); }
        return out;
    }
    private void mergeMoves(int[] a,int lo,int hi,java.util.List<Move> out){if(lo>=hi)return;int mid=(lo+hi)/2;mergeMoves(a,lo,mid,out);mergeMoves(a,mid+1,hi,out);int[] copy=Arrays.copyOfRange(a,lo,hi+1);int l=0,r=mid-lo+1;for(int k=lo;k<=hi;k++){if(l>mid-lo)a[k]=copy[r++];else if(r>hi-lo)a[k]=copy[l++];else{out.add(Move.compare(lo+l,lo+r,"Merge: choose the smaller front ball"));a[k]=(copy[l]<=copy[r])?copy[l++]:copy[r++];}out.add(Move.place(k,a[k],"Merge sorted pieces: place " + a[k] + " in position " + (k+1)));}}
    private static void swap(int[]a,int i,int j){int t=a[i];a[i]=a[j];a[j]=t;}

    private void saveCertificate() {
        String learner=JOptionPane.showInputDialog(this,"Learner name:","Certificate",JOptionPane.QUESTION_MESSAGE); if(learner==null||learner.trim().isEmpty())return;
        JFileChooser chooser=new JFileChooser(); chooser.setSelectedFile(new File("SortSphere-Certificate.png"));
        if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;
        try { BufferedImage image=new BufferedImage(1200,800,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();g.setColor(new Color(247,250,255));g.fillRect(0,0,1200,800);g.setColor(new Color(44,68,155));g.setStroke(new BasicStroke(14));g.drawRect(28,28,1144,744);g.setFont(new Font("Serif",Font.BOLD,64));g.drawString("CERTIFICATE OF ACHIEVEMENT",165,170);g.setFont(new Font("SansSerif",Font.PLAIN,30));g.setColor(Color.DARK_GRAY);g.drawString("This celebrates",505,270);g.setFont(new Font("Serif",Font.BOLD,60));g.setColor(new Color(99,71,166));g.drawString(learner.trim(),Math.max(100,600-g.getFontMetrics().stringWidth(learner.trim())/2),365);g.setFont(new Font("SansSerif",Font.PLAIN,28));g.setColor(Color.DARK_GRAY);g.drawString("for becoming a SortSphere Sorting Champion",260,455);g.drawString("Algorithms mastered: " + Math.max(1,mastered.size()) + " / 4",420,520);g.setFont(new Font("SansSerif",Font.BOLD,22));g.setColor(new Color(44,68,155));g.drawString("Keep comparing. Keep learning.",440,650);g.dispose();ImageIO.write(image,"png",chooser.getSelectedFile());JOptionPane.showMessageDialog(this,"Certificate saved!"); }catch(Exception ex){JOptionPane.showMessageDialog(this,"Could not save: "+ex.getMessage());}
    }

    static class Move { int i,j,value; boolean compare,swap,place; String text; static Move compare(int i,int j,String t){Move m=new Move();m.i=i;m.j=j;m.compare=true;m.text=t;return m;}static Move swap(int i,int j,String t){Move m=compare(i,j,t);m.swap=true;return m;}static Move place(int i,int v,String t){Move m=new Move();m.i=i;m.value=v;m.place=true;m.text=t;return m;} }
    static class BallBoard extends JPanel { int[] values={}; int activeA=-1,activeB=-1; boolean swapping;
        BallBoard(){setPreferredSize(new Dimension(820,330));}
        void setValues(int[] v){values=v.clone();activeA=activeB=-1;repaint();}
        void apply(Move m){activeA=m.i;activeB=m.j;swapping=m.swap;if(m.swap)swap(values,m.i,m.j);if(m.place)values[m.i]=m.value;repaint();}
        protected void paintComponent(Graphics gr){super.paintComponent(gr);Graphics2D g=(Graphics2D)gr.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);int n=values.length;if(n==0)return;int d=Math.min(82,Math.max(44,(getWidth()-60)/n-12)),gap=(getWidth()-n*d)/(n+1),y=getHeight()/2-d/2;for(int i=0;i<n;i++){int x=gap+i*(d+gap);Color c=(i==activeA||i==activeB)?(swapping?new Color(241,101,91):new Color(247,188,66)):new Color(83,130,224);g.setColor(c);g.fillOval(x,y,d,d);g.setColor(Color.WHITE);g.setFont(new Font("SansSerif",Font.BOLD,Math.max(15,d/3)));String s=String.valueOf(values[i]);FontMetrics fm=g.getFontMetrics();g.drawString(s,x+(d-fm.stringWidth(s))/2,y+(d+fm.getAscent()-fm.getDescent())/2);g.setColor(new Color(100,108,128));g.setFont(new Font("SansSerif",Font.PLAIN,12));String p=String.valueOf(i+1);g.drawString(p,x+d/2-g.getFontMetrics().stringWidth(p)/2,y+d+22);}g.dispose();}
    }
    public static void main(String[] args){SwingUtilities.invokeLater(()->new SortSphereGame().setVisible(true));}
}
