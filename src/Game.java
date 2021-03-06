import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Game extends Canvas implements Runnable {

    private final static long serialversionUID = 1L;

    private boolean isRunning = false;
    private Thread thread;
    private Handler handler;
    private BufferedImage level = null;
    private BufferedImage sprite_sheet = null;
    private BufferedImage floor = null;
    public int ammo =3;
    public int hp=100;
    private Camera camera;
    private SpriteSheet ss;

    public Game() throws IOException {
        new Window(1000,563,"lEFT 4 DEAD 3",this);
        start();
        handler= new Handler();
        camera = new Camera(0, 0);
        this.addKeyListener(new KeyInput(handler));

        BufferedImageLoader loader = new BufferedImageLoader();
        level = loader.loadImage("/res/level.png");
        sprite_sheet= loader.loadImage("/res/sprite_sheet.png");

        ss = new SpriteSheet(sprite_sheet);

        floor = ss.grabImage(4,2,32,32);

        this.addMouseListener(new MouseInput(handler,camera,this, ss));
        //handler.addObject(new Box(100, 100, ID.Block));

        loadLevel(level);

    }
    private void loadLevel(BufferedImage image){
        int w = image.getWidth();
        int h = image.getHeight();

        for (int xx= 0 ;xx<w;xx++){
            for (int yy= 0 ; yy<h;yy++){
                int pixel = image.getRGB(xx,yy);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = (pixel) & 0xff;

                if (red ==255){
                    handler.addObject(new Block(xx*32,yy*32,ID.Block,ss));
                }
                if (blue ==255 && green ==0){
                    handler.addObject(new Wizard(xx*32,yy*32,ID.Player,handler,this,ss));
                }
                if (green ==255 && blue ==0){
                    handler.addObject(new Enemy(xx*32,yy*32,ID.Enemy,handler,ss));
                }
                if (green ==255 && blue ==255){
                    handler.addObject(new Crate(xx*32,yy*32,ID.Crate,ss));
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new Game();

    }

    private void start(){
        isRunning=true;
        thread= new Thread(this);
        thread.start();
    }

    private void stop() {
        isRunning = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        System.out.println("o");
        this.requestFocus();
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 2000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;
        while(isRunning){
            long now = System.nanoTime();
            delta+=(now - lastTime) / ns;
            lastTime = now;
            while(delta >= 1){
                tick();
                delta--;
            }
            //if(isRunning)
            render();
            frames++;

            if(System.currentTimeMillis() - timer > 1000){
                timer += 1000;
                frames = 0;
            }
        }
        stop();
    }

    public void tick(){
        for (int i = 0; i < handler.object.size(); i++) {
            if (handler.object.get(i).getID() == ID.Player) {
                    camera.tick(handler.object.get(i));
            }
        }
        handler.tick();
    }
    public void render()
    {
        BufferStrategy bs = this.getBufferStrategy();
        if(bs == null){
            this.createBufferStrategy(3); //when the game is running it will create in advance 3 frames
            return;
        }
        Graphics g = bs.getDrawGraphics();
        Graphics2D g2d = (Graphics2D) g;
        ////////////////////////////////// all the lines of code below this area in charge of rendering/drawing stuff


        g2d.translate(-camera.getX(),-camera.getY());


        for ( int xx=0;xx<30*72;xx+=32){
            for (int yy=0;yy<30*72;yy+=32){
                g.drawImage(floor,xx,yy,null);
            }
        }

        handler.render(g);

        g2d.translate(-camera.getX(),-camera.getY());

        g.setColor(Color.GRAY);
        g.fillRect(5,5,200,32);
        g.setColor(Color.green);
        g.fillRect(5,5,hp*2,32);
        g.setColor(Color.black);
        g.drawRect(5,5,200,32);

        g.setColor(Color.white);
        g.drawString("Ammo: " + ammo,5,50);
        //////////////////////////////////
        g.dispose();
        bs.show();

    }

}
