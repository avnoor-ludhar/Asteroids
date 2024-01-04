//Avnoor Ludhar
//Running.java
//This is a remake of the arcade Asteroids game. In this Asteroids game
//you shoot asteroids that break into smaller pieces until they are finally taken off the screen.
//Everytime the asteroids break it adds 100 points to the score. A boss is implemented at
//level 2 and appears every level after that. It also has small little add-ons such as a menu,
// a highscorers menu, and an input for your score.

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.io.*;
import java.util.Scanner;


//Runs the game and makes the main Asteroids object just puts the game into the frame and fits the contents into the frame. Also makes an object of the game.

public class AsteroidsGame extends JFrame{
    AsteroidsPane game = new AsteroidsPane();

    public AsteroidsGame(){
        super("Welcome to Asteroids!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        add(game);
        pack();    // set the size of my Frame exactly big enough to hold the contents.
        setVisible(true);
    }

    public static void main(String[] args){
        AsteroidsGame frame = new AsteroidsGame();
    }

}



//The main game class used for all the actual elements of the game in code. Contains all the objects and variables that belong to the game.

class AsteroidsPane extends JPanel implements KeyListener, ActionListener, MouseListener{

    //Constants for the different screens.
    public static final int INTRO = 0, GAME = 1, END = 2, HIGHSCORERS = 3;
    //Variable to keep track of the screen.
    private int screen = INTRO;
    //Boolean variables for each key on the keyboard to check if they are pressed or not.
    private boolean []keys;
    //The current level of the game.
    private int level;
    private int playerPoints;
    //Stars are the stars that are drawn on the screen in the intro and end screens.
    private Star [] stars;
    //Counts the asteroids that are added after the 3rd level.
    private int AsteroidCount;


    private final Font font, font2;

    private Timer timer;
    //Images for the game.
    private Image gameBack, title, highscoreback, gameover;
    //Hero object holds all attributes of the hero.
    private Hero hero;
    //highscore object holds all the values of the highscorer menu.
    private highScore highmenu;
    //An arraylist of Asteroid objects holds all the asteroids.
    private ArrayList<Asteroid> asteroids;
    //An arraylist of bullet objects that are used by the ship.
    private ArrayList<Bullet> bullets;
    //An arraylist of bullet objects that are used by the flying saucer.
    private ArrayList<Bullet> enemyBullets;
    //Creates an enemy object.
    private enemyShip enemy;

    //All sound effects
    private final SoundEffect shootSound;
    private final SoundEffect flameThrower;
    private final SoundEffect collision;
    private final SoundEffect death;

    //Constructs the Game with all the objects, variables fonts and pictures. Adds the mouselistener, actionlistener, and key listener. Also,
    //Sets the value for each Star object to be moved.
    public AsteroidsPane(){
        gameBack = new ImageIcon("nebula.jpg").getImage();
        title = new ImageIcon("Asteroids_arcade_logo.png").getImage();
        highscoreback = new ImageIcon("galaxy.jpg").getImage();
        gameover = new ImageIcon("GameOver.jpg").getImage();
        setPreferredSize(new Dimension(getWidth(), getHeight()));
        setFocusable(true);
        requestFocus();
        addMouseListener(this);
        addKeyListener(this);
        keys = new boolean[KeyEvent.KEY_LAST+1];
        AsteroidCount = 0;

        hero = new Hero();
        bullets = new ArrayList<Bullet>();
        enemyBullets = new ArrayList<Bullet>();
        asteroids = new ArrayList<Asteroid>();
        highmenu = new highScore();

        enemy = new enemyShip();

        playerPoints = 0;

        font = new Font("Futura", Font.BOLD, 37);
        font2 = new Font("Futura", Font.BOLD, 65);

        stars = new Star[130];
        setStars();

        shootSound = new SoundEffect("Sounds/ShootSound.wav");
        flameThrower = new SoundEffect("Sounds/Using Flame Thrower Sound Effect.wav");
        collision = new SoundEffect("Sounds/Rock break sound effect.wav");
        death = new SoundEffect("Sounds/Glass Shattering Sound Effect.wav");

        timer = new Timer(20, this);
        timer.start();
    }

    //The actionperformed method that checks for all actions happening on the screen.
    @Override
    public void actionPerformed(ActionEvent e){
        screenCheck();
        repaint();
    }

    //Checks what screen you are on and calls methods accordingly. If you are in the intro or end screen
    //it moves the stars we made earlier to give a sci-fi look to the screen. If you are in the game it calls all methods
    //of the game.
    public void screenCheck(){
        if(screen == INTRO){
            Star.move(stars);

        } else if(screen == GAME){
            gameChanges();
            asteroids = Asteroid.advanceAsteroids(asteroids);
            checkCollision();
            changeLevel();

        } else if(screen == END){
            Star.move(stars);
        }
    }

    //Random number between the low and the high.
    public static int randint(int low, int high){
        return (int)(Math.random()*(high-low+1)+low);
    }


    //Checks collisions between the hero's bullets and asteroids. It does it by looping through both arraylists
    //and checks if the point of the bullet hits the polygon of the asteroid. If it does hit, it deletes both the bullet and the asteroid from the
    //arraylist, plays the death sound, and adds points to the playersPoints. If the enemy is alive it also calls the collisions relating to the enemy.

    private void checkCollision(){
        //Break checks if it breaks out of the third loop to make sure it also breaks out of the between points loop.
        //The between points just gets the 15 points between the updates of the bullets to get a more accurate collision.

        boolean Break = false;
        for(int b = 0; b < bullets.size(); b++) {
            int[][] betweenPoints = Bullet.checkBetween(bullets.get(b));
            for (int b2 = 0; b2 < betweenPoints[0].length; b2++) {
                Point bulletPoint = new Point(betweenPoints[0][b2], betweenPoints[1][b2]);
                for (int i = 0; i < asteroids.size(); i++) {
                    if (asteroids.get(i).checkCollision(asteroids.get(i).getPolygon(), bulletPoint)) {
                        if (asteroids.get(i).getSize() != asteroids.get(i).SMALL) {
                            Asteroid[] newAsteroids = asteroids.get(i).Break();
                            asteroids.remove(i);
                            asteroids.add(newAsteroids[0]);
                            asteroids.add(newAsteroids[1]);

                        } else {
                            asteroids.remove(i);
                        }
                        playerPoints += 100;
                        bullets.remove(b);
                        collision.play(3);
                        Break = true;
                        break;
                    }
                }
                if(Break == true){
                    Break = false;
                    break;
                }
            }
        }
        if(enemy.getenemyStatus() == true){
            checkCollision2();
            checkCollision3();
        }
    }


    //Checks collisions between the enemy bullets and the asteroids the same way as it did in the first checkCollision method.

    private void checkCollision2(){
        for(int b = 0; b < enemyBullets.size(); b++){
            Point bulletPoint = new Point(enemyBullets.get(b).getX(), enemyBullets.get(b).getY());
            for(int i = 0; i<asteroids.size(); i++){
                if(asteroids.get(i).checkCollision(asteroids.get(i).getPolygon(), bulletPoint)){
                    if(asteroids.get(i).getSize() != asteroids.get(i).SMALL){
                        Asteroid [] newAsteroids = asteroids.get(i).Break();
                        asteroids.remove(i);
                        asteroids.add(newAsteroids[0]);
                        asteroids.add(newAsteroids[1]);

                    }else {
                        asteroids.remove(i);
                    }
                    enemyBullets.remove(b);
                    collision.play(3);
                    break;
                }
            }
        }
    }

    //This checks the collisions between the hero's bullets and the enemy the same way checkCollision does.
    private void checkCollision3(){
        boolean Break = false;
        for(int b = 0; b < bullets.size(); b++){
            Polygon poly = enemy.getEnemy();
            int [][] betweenPoints = Bullet.checkBetween(bullets.get(b));
            for(int i = 0; i<betweenPoints[0].length; i++){
                Point bulletPoint = new Point(betweenPoints[0][i], betweenPoints[1][i]);
                if(poly.contains(bulletPoint)){
                    bullets.remove(b);
                    death.play(3);
                    playerPoints += 500;
                    enemy.setenemyStatus(false);
                    Break = true;
                    break;
                }
            }
            if(Break == true){
                Break = false;
                break;
            }
        }
    }


    //Calls all the methods that change something in the game. Calls all the methods from the hero, enemy, and plays sounds accordingly.

    public void gameChanges(){
        //Calls the hero's shoot method.
        if(keys[KeyEvent.VK_SPACE]){
            bullets = hero.shoot(bullets);
            shootSound.play(2);
        }

        if(enemy.getenemyStatus() == true){
            enemyBullets = enemy.shoot(enemyBullets, hero);
            enemyBullets = hero.heroCollisions(enemyBullets, death);
            enemyBullets = Bullet.shootAdvance(enemyBullets);
            hero.heroDead2(death, enemy);
        }

        bullets = Bullet.shootAdvance(bullets);

        keys = hero.heroMoves(keys, flameThrower);
        hero.heroDead(asteroids, death);

        //Changes screen if the hero has no lives left.
        if(hero.getLives() == 0){
            screen = END;
        }

        enemy.changeAngle();
        enemy.enemyAdvance(5);
    }

    //Checks the level and sets the asteroids and the enemy.
    private void levelCheck() {
        if (level == 1) {
            setAsteroids(4);

        } else if (level == 2) {
            setAsteroids(5);
            enemy.setenemyStatus(true);
        } else if (level == 3) {
            setAsteroids(6);
            enemy.setenemyStatus(true);
        } else{
            setAsteroids(7 + AsteroidCount);
            AsteroidCount += 1;
            enemy.setenemyStatus(true);
        }
    }

    //Event of when the key is released.
    @Override
    public void keyReleased(KeyEvent ke){
        int key = ke.getKeyCode();
        keys[key] = false;
    }

    //Event when the key is pressed.
    @Override
    public void keyPressed(KeyEvent ke){
        int key = ke.getKeyCode();
        keys[key] = true;
    }
    @Override
    public void keyTyped(KeyEvent ke){

    }

    @Override
    public void mouseClicked(MouseEvent e){}
    @Override
    public void mouseEntered(MouseEvent e){}
    @Override
    public void mouseExited(MouseEvent e){}
    @Override
    //Event of when the mouse is pressed. This method changes the screen according to what screen you are and where you click on the screen.
    //When going to the game it sets the level and calls to check the level. In the end screen if you hit the restart button it resets all objects
    //of the game and checks and adds the score if it is a high score to the text file.

    public void mousePressed(MouseEvent e){
        if(screen == INTRO){
            if(e.getX() > 630 && e.getX()<800 && e.getY() > 400 && e.getY()<475){
                screen = GAME;
                level = 1;
                levelCheck();
            }
            if(e.getX() > 500 && e.getX()<917 && e.getY() > 500 && e.getY()<575){
                screen = HIGHSCORERS;
            }
        }else if(screen == HIGHSCORERS){
            if(e.getX() > 50 & e.getX()<255 && e.getY() >625 && e.getY()<700){
                screen = INTRO;
            }
        }

        else if(screen == END) {
            highmenu.ScoreCheck(playerPoints);
            try{
                highmenu.WritetoFile();
            }catch(IOException V){
            }

            if(e.getX() > 50 & e.getX()<325 && e.getY() >625 && e.getY()<700){
                screen = INTRO;
                level = 1;
                playerPoints = 0;
                hero = new Hero();
                asteroids = new ArrayList<Asteroid>();
                enemy = new enemyShip();
                highmenu = new highScore();
                bullets = new ArrayList<Bullet>();
                enemyBullets = new ArrayList<Bullet>();
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e){}

    //Paint method calls all the drawing methods according to the screen and puts them onto the screen.
    @Override
    public void paint(Graphics g) {
        if (screen == INTRO) {
            introDraw(g);
        } else if (screen == GAME) {
            gameDraw(g);

        } else if (screen == HIGHSCORERS) {
            highscorersDraw(g);

        } else if (screen == END) {
            endScreenDraw(g);
        }
    }

    //Draws out all the pictures and maeks the restart button. Also implements the hover of the restart button
    //if the mouse position is on the rectangle it changes the font of the button to green.

    public void endScreenDraw(Graphics g){
        g.setFont(font2);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.drawImage(gameover, 500,200, this);

        Rectangle homebutton = new Rectangle(50, 625, 275, 75);
        g.drawString("Restart", 50, 700);

        Star.drawStars(stars, g);

        try {
            if (homebutton.contains(getMousePosition())) {
                g.setColor(Color.green);
                g.drawString("Restart", 50, 700);
            }
        } catch (Exception e) {

        }
    }

    //Draws everything on the highscorer menu, draws out all the values on the menu including text and the home button. The home button
    //glows the same way as the restart button in the end screen.

    public void highscorersDraw(Graphics g){
        try {
            highmenu.HighscoreDraw(g, highscoreback, font, font2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        g.setFont(font2);
        g.setColor(Color.WHITE);
        g.drawString("Home", 50, 700);

        Rectangle homebutton = new Rectangle(50, 625, 200, 75);

        try {
            if (homebutton.contains(getMousePosition())) {
                g.setColor(Color.green);
                g.drawString("Home", 50, 700);
            }
        } catch (Exception e) {

        }
    }

    //Draws everything that is in the game screen. Draws the image in the back, and all the text. Also, calls all methods that draw the
    //different elements of the game like the asteroids, bullets and the hero.

    public void gameDraw(Graphics g){
        Graphics2D g2d = (Graphics2D)g;

        g.drawImage(gameBack,0,0,null);
        keys = hero.drawHero(g, keys);

        g.setColor(Color.GRAY);

        for(int i = 0; i<asteroids.size(); i++) {
            g2d.fill(asteroids.get(i).getPolygon());
        }

        g.setColor(Color.WHITE);
        Bullet.bulletDraw(g2d, bullets);
        g.setColor(Color.RED);
        Bullet.bulletDraw(g2d,enemyBullets);

        g.setColor(Color.WHITE);
        g.setFont(font);
        g.setColor(new Color(206, 184, 184));
        g.drawString("Points: " + playerPoints, 400, 40);
        g.drawString("Level: " + level, 700,40);

        enemy.drawEnemy(g);
    }

    //Draws all the elements of the introscreen, including the picture, the text, and all the buttons and hovers. Also draws the stars that
    //create the sci-fi look.

    public void introDraw(Graphics g){
        g.setColor(new Color(0,0,0));
        g.fillRect(0,0,getWidth(), getHeight());
        g.setColor(Color.WHITE);
        Star.drawStars(stars, g);
        Rectangle playbutton = new Rectangle(630, 400,150, 75);
        Rectangle highscorersButton = new Rectangle(510, 500,408, 75);

        g.setFont(font2);
        g.setColor(Color.WHITE);
        g.drawString("Play", 630, 450);
        g.drawString("Highscorers", 510, 550);

        try{
            if(playbutton.contains(getMousePosition())){
                g.setColor(Color.green);
                g.drawString("Play", 630, 450);
            }

        }catch(Exception e){
        }

        try{
            if(highscorersButton.contains(getMousePosition())){
                g.setColor(Color.green);
                g.drawString("Highscorers", 510, 550);
            }
        }catch(Exception e){

        }

        g.drawImage(title, 450, 200,this);
    }

    //Adds asteroids in based on a size. Just loops through the size and adds an asteroid object to the arraylist.

    public void setAsteroids(int size){
        for(int i = 0; i<size; i++){
            asteroids.add(new Asteroid(Asteroid.LARGE, Asteroid.LARGES));
        }
    }

    //changes the level and checks for the new level when there are no asteroids left.

    public void changeLevel(){
        if(asteroids.size() == 0){
            level += 1;
            levelCheck();
        }
    }

    //Adds stars to the stars array by looping through the length of the stars array and adding in objects.
    private void setStars(){
        int speed = 17;
        for(int i = 0; i<stars.length; i++){
            stars[i] = new Star(randint(0,1500), randint(0,800), randint(1,5), speed);
        }
    }
}


//The hero class has all of the methods and values that belong to the hero/spaceship.
class Hero {

    //angle the ship is moving at.
    private int angle;
    //x,y position of the ship.
    private int x,y;
    //the change in x and y.
    private double vx;
    private double vy;
    //the lives of the hero.
    private int lives;

    //3 timers one for the players shot, one for the teleport, and the timer for when the hero dies.
    private int shootTimer;
    private int teleTime;
    private int deadCount;

    //Font of printing the lives.
    private Font liveFont;

    //Constructs all values, sets the position to the centre, and sets all of the fields.

    public Hero(){
        x = 750;
        y = 400;
        vx = 0;
        vy = 0;
        angle = 90;
        shootTimer = 10;
        teleTime = 25;
        lives = 5;
        liveFont = new Font("Futura", Font.BOLD, 37);
        deadCount = 0;
    }

    //Getters for the lives, the x of the hero, and the y of the hero.
    public int getLives(){
        return lives;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    //This method checks the teleport timer and sets the x and y to a random location.
    private void teleport(){
        if(teleTime == 0){

            x = AsteroidsPane.randint(0,1500);
            y = AsteroidsPane.randint(0,800);

            teleTime = 25;
        }
    }

    //Finds the cos and sin of the angle and multiplies it by a constant, to get the velocity of the ship. Causes the glinding and thrusting
    //of the ship. Caps the speed of at a speed of 8.

    private void thrust() {
        if(Math.abs(vx)<8 && Math.abs(vy)<8){
            vx += Math.cos(Math.toRadians(angle))*1.2;
            vy += Math.sin(Math.toRadians(angle))*1.2;
        }
    }

    //Checks all the movements of the hero. So when the keys are pressed what methods should be called according to that. For example if the
    //up arrow key is pressed the ship should thrust and play the sound of the thrust. Calls the counts for all the timers.

    public boolean [] heroMoves(boolean [] keys, SoundEffect sound){

        if(keys[KeyEvent.VK_UP]){
            sound.play(2000);
            thrust();
        }
        if(keys[KeyEvent.VK_LEFT]){
            slow();
            turn(8);
        }if(keys[KeyEvent.VK_RIGHT]){
            slow();
            turn(-8);
        }
        if(keys[KeyEvent.VK_DOWN]){
            teleport();
        }
        else {
            slow();
        }
        advance();
        count();

        return keys;
    }

    //Slows down the ship and stops it if the velocity gets below a certain threshold.
    private void slow() {
        vx *= 0.987;
        vy *= 0.987;
        if(Math.abs(vx) < .25){
            vx = 0;
        }
        if(Math.abs(vy) < .25){
            vy = 0;
        }
    }

    //Moves according to the velocity and checks if it on the screen. If the position goes off of the screen it's
    //position gets set to the other side of the screen.

    private void advance(){
        if(x < 1505 && x> -5 && y < 805 && y > -5){
            x += vx;
            y -= vy;
        }
        if(x > 1500){
            x = 0;
        } else if(x < 0){
            x = 1500;
        } else if(y > 800){
            y = 0;
        } else if(y < 0) {
            y = 800;
        }
    }

    //Turns a polar point to a point and returns it.
    private Point xy(double ang, double mag){
        int px = (int)(x + Math.cos(Math.toRadians(ang)) * mag);
        int py = (int)(y - Math.sin(Math.toRadians(ang))*mag);
        return new Point(px,py);
    }

    //Turns a polar point at a specific spot to a normal point. Used to draw the lives at the top.
    private Point livesDrawPoint(double ang, double mag, int x, int y){
        int px = (int)(x + Math.cos(Math.toRadians(ang)) * mag);
        int py = (int)(y - Math.sin(Math.toRadians(ang))*mag);
        return new Point(px,py);
    }

    //gets the points of the ships that are on the top of the screen in the game for the lives and returns them.
    public int [][] livesDraw(int x, int y){
        int [] px = new int[4];
        int [] py = new int[4];
        int [][] points = new int[2][4];

        Point p1 = livesDrawPoint(90, 17,x,y);
        px[0] = p1.x;
        py[0] = p1.y;
        p1 = livesDrawPoint(90+135, 22,x,y);
        px[1] = p1.x;
        py[1] = p1.y;
        p1 = livesDrawPoint(90, -7,x,y);
        px[2] = p1.x;
        py[2] = p1.y;
        p1 = livesDrawPoint(90-135, 22, x,y);
        px[3] = p1.x;
        py[3] = p1.y;

        points[0] = px;
        points[1] = py;

        return points;
    }

    //Draws everything that belongs to the hero. This includes the lives and the drawings for the lives. Calls all methods that relate to graphics
    //or getting points for polygons. Returns the keys to make sure the keys that were pressed by the user.

    public boolean[] drawHero(Graphics g, boolean[]keys){

        Graphics2D g2d = (Graphics2D)g;
        g.setColor(new Color(206, 184, 184));

        g.setFont(liveFont);
        g.drawString("Lives: ", 900, 40);


        //draws out the pictures for the lives.
        for(int i = 1; i<=lives; i++){
            int [][] liveDraw = livesDraw(1005 + 38 * i, 27);
            Polygon poly = new Polygon(liveDraw[0], liveDraw[1], 4);
            g2d.fill(poly);
        }

        g.setColor(Color.WHITE);
        int [][] polyPoints = getHero();
        Polygon poly = new Polygon(polyPoints[0], polyPoints[1], 4);

        //blinks the picture every so often according to the death timer. Which causes an animation.
        if(deadCount > 0){
            if(deadCount % 4 == 0){
                g2d.fill(poly);
                if(keys[KeyEvent.VK_UP]){
                    g.setColor(Color.RED);
                    g2d.fill(thrustDraw());
                }
            }
            deadCount -= 0.3;
        }
        else if(deadCount == 0){
            g2d.fill(poly);
            if(keys[KeyEvent.VK_UP]){
                g.setColor(Color.RED);
                g2d.fill(thrustDraw());
            }
        }

        return keys;
    }

    //This method adds bullets to the hero's bullets. It adds according to the x position adn y position of the hero. And the
    //angle the ship was facing. It returns the arraylist of the bullets to be changed in the main game class.

    public ArrayList shoot(ArrayList<Bullet> bullets){
        if(bullets.size()<6 && shootTimer == 0){
            Point xy = xy(angle, 17);
            bullets.add(new Bullet(xy.x,xy.y,angle));
            shootTimer = 10;
        }
        return bullets;
    }

    //counts down the timers if they are above 0.
    private void count(){
        if (shootTimer > 0) {
            shootTimer -= 1;
        }

        if(teleTime > 0){
            teleTime -=1;
        }
    }

    //Turns the ship a certain angle.
    private void turn(int ang){
        angle = (angle + ang % 360 + 360) % 360;
    }

    //Gets the points of the polygon for the hero. It used the xy method to get the outer points of the hero based on
    //the centre which was the position of the hero.

    public int [][] getHero(){
        int [] px = new int[4];
        int [] py = new int[4];
        int [][] points = new int[2][4];

        Point p1 = xy(angle, 17);
        px[0] = p1.x;
        py[0] = p1.y;
        p1 = xy(angle+135, 22);
        px[1] = p1.x;
        py[1] = p1.y;
        p1 = xy(angle, -7);
        px[2] = p1.x;
        py[2] = p1.y;
        p1 = xy(angle-135, 22);
        px[3] = p1.x;
        py[3] = p1.y;

        points[0] = px;
        points[1] = py;

        return points;
    }

    //Gets the polygon for the thrust behind the ship. It used the xy method to get the outer points of the thrutst based on
    //the centre which was the position of the hero. Returns the polygon of the thrust.
    public Polygon thrustDraw(){
        int [] px = new int[4];
        int [] py = new int[4];
        int [][] points = new int[2][4];

        Point p1;
        p1 = xy(angle+135, 22);
        px[0] = p1.x;
        py[0] = p1.y;

        p1 = xy(angle, -7);
        px[1] = p1.x;
        py[1] = p1.y;

        p1 = xy(angle-135, 22);
        px[2] = p1.x;
        py[2] = p1.y;

        p1 = xy(angle, -30);
        px[3] = p1.x;
        py[3] = p1.y;

        points[0] = px;
        points[1] = py;

        Polygon poly = new Polygon(points[0], points[1], 4);
        return poly;
    }

    //Converts the points of the hero which are gotten from the get hero method to Point objects which are stored in a array. Then checks if
    //those points collide with the asteroids to see if the hero dies. If the hero isn't in respawn mode it will reset the hero with one less life.

    public boolean heroDead(ArrayList<Asteroid> asteroids, SoundEffect death){
        int [][] checkPoints = getHero();
        int [] xpoints = checkPoints[0];
        int [] ypoints = checkPoints[1];

        Point [] points = new Point[4];

        for(int i = 0; i< xpoints.length; i++){
            Point point = new Point(xpoints[i], ypoints[i]);
            points[i] = point;
        }

        for(int a = 0; a<asteroids.size(); a++){
            for(int p = 0; p<points.length; p++){
                Polygon poly = asteroids.get(a).getPolygon();
                if(poly.contains(points[p])){
                    if(deadCount> 0){

                    }else if(deadCount == 0){
                        death.play(0);
                        lives -= 1;
                        x = 750;
                        y = 400;
                        vx = 0;
                        vy = 0;
                        deadCount = 140;
                        return true;
                    }
                }
            }
        }

        return false;
    }

    //Checks collisions between the points of the hero and the enemy to see if the hero dies. Uses the same logic as
    //the heroDead method.
    public boolean heroDead2( SoundEffect death, enemyShip Enemy){

        int [][] checkPoints = getHero();
        int [] xpoints = checkPoints[0];
        int [] ypoints = checkPoints[1];

        Point [] points = new Point[4];

        for(int i = 0; i< xpoints.length; i++){
            Point point = new Point(xpoints[i], ypoints[i]);
            points[i] = point;
        }

        for(int i = 0; i<points.length; i++){
            Polygon Enemycheck = Enemy.getEnemy();
            if(Enemycheck.contains(points[i])){
                if(deadCount> 0){
                }else if(deadCount == 0){
                    death.play(0);
                    lives -= 1;
                    x = 750;
                    y = 400;
                    vx = 0;
                    vy = 0;
                    deadCount = 140;
                    return true;
                }
            }
        }
        return false;
    }

    //heroCollsions checks the collisions between an arraylist of bullets(enemyBullets), and checks if they contact the polygon of the hero.
    //Uses the same logic of collisions between bullets and asteroids in the checkCollisions method. Returns the bullets to be changed in the main game class.
    public ArrayList<Bullet> heroCollisions(ArrayList<Bullet> bullets, SoundEffect death){
        for(int b = 0; b<bullets.size(); b++){
            int [][]points  = getHero();
            Polygon P = new Polygon(points[0], points[1], 4);
            int [][] betweenPoints = Bullet.checkBetween(bullets.get(b));
            for(int i = 0; i<betweenPoints[0].length; i++){
                Point bulletPoint = new Point(betweenPoints[0][i], betweenPoints[1][i]);
                if(P.contains(bulletPoint)){
                    if(deadCount> 0){
                    }else if(deadCount == 0){
                        bullets.remove(b);
                        death.play(0);
                        lives -= 1;
                        x = 750;
                        y = 400;
                        vx = 0;
                        vy = 0;
                        deadCount = 140;
                        return bullets;
                    }
                }
            }
        }
        return bullets;
    }
}

//Class for all the aspects of the game that belong to the a singular asteroid.
class Asteroid{

    //x,y position of the asteroid.
    private int x, y;
    //Size of the asteroid.
    private int size;
    //Radius of the asteroid.
    private int radius;
    //The angle that the asteroids move at.
    private int MoveAngle;
    //Constants for the 3 sizes of the
    public static final int LARGE = 5;
    public final int MED = 3;
    public final int SMALL = 2;
    //The points of the original asteroid.
    private int[][] points;
    //The angle at which the asteroid turns at.
    private int turnAngle;
    //The speed at which the asteroid is moving at.
    private int speed;
    //Constants for the speed of the different sizes of asteroids.
    public static final int LARGES = 3;
    public static final int MEDS = 4;
    public static final int SMALLS = 5;

    //Constructs the asteroid with all the fields. Takes in the size and speed as a parameter, the constructor
    //also calls the createAsteroid method to get the points of the polygon.
    public Asteroid(int size, int speed){
        x = AsteroidsPane.randint(0, 700);
        y = AsteroidsPane.randint(0, 300);
        this.size = size;
        radius= 20 * size;
        points = createAsteroid();
        MoveAngle = AsteroidsPane.randint(0,359);
        turnAngle = 0;
        this.speed = speed;
    }

    public int getSize(){
        return size;
    }

    //Gets the transformed points of the original asteroid that was constructed. by turning the points to polar and changing the points
    //according to the turnAngle. Also, returns a polygon of the new points.

    public Polygon getPolygon(){
        int px[] =  new int[points[0].length];
        int py[] = new int[points[1].length];

        for(int i = 0; i< px.length; i++){
            px[i] = points[0][i] + x;
            py[i] = points[1][i] + y;
        }

        double [][]polarPoints = toPolar(px, py);

        int [][] transPoints = asteroidTurn(polarPoints, turnAngle);

        px = transPoints[0];
        py = transPoints[1];

        Polygon p = new Polygon(px, py, 9);

        return p;
    }

    //Creates an asteroid by creating points based on an angle and getting random radius's to the edges of the asteroid. Returns the points constructed.
    private int[][] createAsteroid(){
        int angle = 0;
        int[][] allpoints = new int[2][9];
        int [] xpoints = new int[9];
        int [] ypoints = new int[9];
        for(int i = 0 ; i<9; i ++){
            if(angle > 360){
                break;
            }
            xpoints[i] = (int)(Math.cos(Math.toRadians(angle)) * AsteroidsPane.randint(radius/5, radius/2));
            ypoints[i] = (int)(Math.sin(Math.toRadians(angle)) * AsteroidsPane.randint(radius/5, radius/2));
            angle += 40;
        }

        allpoints[0] = xpoints;
        allpoints[1] = ypoints;
        return allpoints;
    }

    //Turns the points according to the angle by taking in the points in polar notation and adding an angle to it.
    //then converting them back to normal points and returns them.
    private int [][] asteroidTurn(double [][] polarPoints, int Angle){

        int [] xPoints = new int[polarPoints.length];
        int [] yPoints = new int[polarPoints.length];

        //adds angle to the polar point.
        for(int i = 0; i < polarPoints.length; i ++){
            polarPoints[i][1] += Angle;
        }

        //converts back to normal points.
        for(int i = 0; i< polarPoints.length; i++){
            Point toxy = xy(polarPoints[i][1], polarPoints[i][0]);
            xPoints[i] = toxy.x;
            yPoints[i] = toxy.y;
        }
        int [][] newPoints = {xPoints,yPoints};
        return newPoints;
    }


    //Converts normal points to polar points and returning the new points as a double 2D array.
    private double [][] toPolar(int []xPoints, int[]yPoints){

        double [][] polarPoints = new double[xPoints.length][2];

        //Converts points to polar by getting the magnitude from using the pythagorean theorem. And getting the angle by finding the arc tangent of the
        //points then converting it to degrees.
        for(int i = 0; i< xPoints.length; i++){
            int xamount = xPoints[i] - x;
            int yamount = yPoints[i] - y;
            double length = Math.hypot(xamount, yamount);
            polarPoints[i][0] = length;
            double angle = Math.atan2(xamount, yamount);
            angle = angle * 180/Math.PI;
            polarPoints[i][1] = angle;
        }

        return polarPoints;
    }

    //Converts point from polar to a normal point.
    private Point xy(double ang, double mag){
        int px = (int)(x + Math.cos(Math.toRadians(ang)) * mag);
        int py = (int)(y - Math.sin(Math.toRadians(ang))*mag);
        return new Point(px,py);
    }

    //Moves the asteroid according to a velocity which is the change in x and y. If it goes off the screen the position gets set to the other side
    //of the screen. Also, adds an angle to the turning angle to make the asteroid turn.
    public void asteroidAdvance(double dist){
        int changeX = 0;
        int changeY = 0;
        turnAngle += 4;

        if(x < 1510 && x > -10 && y < 810 && y > -10){
            changeX += Math.cos(Math.toRadians(MoveAngle))*dist;
            changeY -= Math.sin(Math.toRadians(MoveAngle))*dist;
            x += changeX;
            y -= changeY;
        } if(x > 1500){
            x = 0;

        } else if(x < 0){
            x = 1500;
        } else if(y > 800){
            y = 0;
        } else if(y < 0) {
            y = 800;
        }
    }

    //Checks if a point is in a polygon.
    public static boolean checkCollision(Polygon poly, Point xy){
        if(poly.contains(xy)){
            return true;
        }
        return false;
    }

    //This method breaks an asteroid into 2 smaller asteroids. sets there size according to the intial size of the asteroid that is used in the method.
    // then sets the angle off 30 degrees to the right and left of the direction the asteroid was going in. Returns the new asteroids as an array.
    public Asteroid [] Break(){
        Asteroid [] smallAsteroids = new Asteroid[2];

        if(size == LARGE){
            smallAsteroids[0] = new Asteroid(MED, MEDS);
            smallAsteroids[1] = new Asteroid(MED, MEDS);
        }
        else if(size == MED){
            smallAsteroids[0] = new Asteroid(SMALL, SMALLS);
            smallAsteroids[1] = new Asteroid(SMALL,SMALLS);
        }
        smallAsteroids[0].MoveAngle = MoveAngle + 30;
        smallAsteroids[1].MoveAngle = MoveAngle - 30;

        smallAsteroids[0].x = x;
        smallAsteroids[1].x = x;

        smallAsteroids[0].y = y;
        smallAsteroids[1].y = y;

        return smallAsteroids;
    }

    //Moves all the asteroids in the main game object according to the speed. By calling the asteroidAdvance method. Then returns the Asteroids.
    public static ArrayList<Asteroid> advanceAsteroids(ArrayList<Asteroid> asteroids){
        for(int i = 0; i<asteroids.size(); i++){
            asteroids.get(i).asteroidAdvance(asteroids.get(i).speed);
        }
        return asteroids;
    }
}


//Class for all the elements that belong to the Bullet.
class Bullet{
    //x,y position of the bullet.
    private int x,y;
    //Angle that the bullet is moving at.
    private int angle;
    //The distance the bullet can travel.
    private int distance;

    //Constructs the bullet according to the parameters that are passed in. Makes the position according to the hero's polygons front.
    public Bullet(int x, int y, int angle){
        this.x = x + (int) Math.cos(Math.toRadians(angle)) * 5;
        this.y = y - (int) Math.sin(Math.toRadians(angle)) * 5;
        this.angle = angle;
        distance = 470;
    }


    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }


    //Moves all the bullets in the arraylist using the bulletAdvance method and returns them to be changed in the main class.
    public static ArrayList<Bullet> shootAdvance(ArrayList<Bullet> bullets){
        for(int i = 0; i < bullets.size(); i++){
            bullets.get(i).bulletAdvance();
            bullets = Bullet.BulletOffScreen(bullets);
        }
        return bullets;
    }

    //Moves the bullet according to an angle and a constant of 15. Distance also decreases
    public void bulletAdvance(){
        if(distance > 0){
            x += Math.cos(Math.toRadians(angle))*15;
            y -= Math.sin(Math.toRadians(angle))*15;
            distance -= 10;
        }else if(distance == 0){
        }
    }

    //This method makes 15 points inbetween the bullet and adds them to a 2D array of points and returns them.
    public static int[][] checkBetween(Bullet bullet){

        int [][] points = new int[2][15];

        int x = bullet.x;
        int y = bullet.y;
        for(int i = 0; i<15; i++){
            x += Math.cos(Math.toRadians(bullet.angle));
            y += Math.sin(Math.toRadians(bullet.angle));
            points[0][i]= x;
            points[1][i]= y;
        }

        return points;
    }

    //Draws each bullet taking in the graphics object and an arraylist of bullets.
    public static void bulletDraw(Graphics g, ArrayList<Bullet> bullets){
        for(int i = 0; i < bullets.size(); i++){
            g.fillOval(bullets.get(i).x, bullets.get(i).y, 7, 7);
        }
    }

    //Takes in an arraylist of bullets and then checks if the position goes off the screen and sets it to the other side of the screen. Then returns
    //the bullets to be changed in the main game class.

    private static ArrayList<Bullet> BulletOffScreen(ArrayList<Bullet> bullets){
        for(int i = 0; i < bullets.size(); i++) {
            if (bullets.get(i).x > 1500) {
                bullets.get(i).x = 0;
            } else if (bullets.get(i).x < 0) {
                bullets.get(i).x = 1500;
            } else if (bullets.get(i).y > 800) {
                bullets.get(i).y = 0;
            } else if (bullets.get(i).y < 0) {
                bullets.get(i).y = 800;
            }
            if(bullets.get(i).distance <= 0){
                bullets.remove(i);
            }
        }
        return bullets;
    }
}

//From Daud I liked the way his stars look so I'm going to use his class for mine.
class Star{
    int x; // x pos of star
    int y; // y pos of star
    int r; // radius of star
    int speed; // speed of star

    public Star(int x, int y, int r, int speed){
        this.x = x;
        this.y = y;
        this.r = r;
        this.speed = speed;
    }


    public static void move(Star[] s){
        for(int i=0; i<s.length; i++){ // move all stars down
            s[i].y += s[i].speed;
        }
        for(int i=0; i<s.length; i++){
            if(s[i].y - s[i].r >= 800){ // if star goes below screen, reset to top with random x
                s[i].y = 0 - s[i].r;
                s[i].x = AsteroidsPane.randint(0,1500);
            }
        }
    }

    //Draws an oval for each star.
    public static void drawStars(Star [] stars, Graphics g){
        for(int i = 0; i< stars.length; i++){
            g.fillOval(stars[i].x, stars[i].y, stars[i].r*2, stars[i].r *2);
        }
    }
}


class SoundEffect{
    private Clip c;

    //creates an object of the file of the sound effect.
    public SoundEffect(String filename){
        setClip(filename);
    }

    //Sets the Audio file to be used.
    public void setClip(String filename){
        try{
            File f = new File(filename);
            c = AudioSystem.getClip();
            c.open(AudioSystem.getAudioInputStream(f));
        } catch(Exception e){ System.out.println("error"); }
    }
    //plays the sound effect.
    public void play(int position){
        c.setFramePosition(position);
        c.start();
    }
}

//Class for the highscorermenu.
class highScore {
    //Boolean variable for writing to the text file.
    private static boolean check;
    //Names from the text file.
    private String [] names;
    //Scores from the text file.
    private int [] scores;

    public highScore(){
        check = false;
        names = new String[5];
        scores = new int[5];
        try{
            LoadScores();    //Loads the scores to the arrays.
        }catch(IOException e){
        }
    }

    //
    public void LoadScores() throws FileNotFoundException {
        Scanner inFile = new Scanner(new BufferedReader(new FileReader("names.txt")));
        int count = 0;
        String [] numbers = new String[5];

        while(inFile.hasNext()){
            if(count % 2 == 0) {
                names[count/2] = inFile.nextLine();
            }
            else if(count % 2 == 1){
                numbers[count/2] = inFile.nextLine();
            }
            count += 1;
        }
        inFile.close();
        for(int i = 0; i<numbers.length; i++){
            scores[i] = Integer.parseInt(numbers[i]);
        }
    }

    //Checks if the score is greater than any of the scores in the file. Opens a dialogue box for the input from the user.
    public void ScoreCheck(int playerPoints){

        if(check == false){
            String name = "";
            name = JOptionPane.showInputDialog("Name:");

            //Creates 2 arrays to mimic the scores field and names field to change the menu.
            int [] newScores = new int[scores.length];
            String [] newnames = new String[names.length];


            for(int i = 0; i<scores.length; i++){
                if(playerPoints > scores[i]){
                    //Makes sure that the scores shift down if the value is in the highscorer menu.
                    for(int b = i+1; b<scores.length; b++){
                        newScores[b] = scores[b-1];
                        newnames[b] = names[b-1];
                    }
                    newScores[i] = playerPoints;
                    newnames[i] = name;
                    scores = newScores;
                    names = newnames;
                    break;
                }else{
                    newScores[i] = scores[i];
                    newnames[i] = names[i];
                }

            }
        }
    }


    //Writes the values to the text file at the end of the game after the user types in their name.
    public void WritetoFile() throws IOException{
        if(check == false){
            PrintWriter outFile = new PrintWriter(
                    new BufferedWriter (new FileWriter ("names.txt")));

            for(int i = 0; i<names.length; i ++){
                outFile.println(names[i].toUpperCase());
                outFile.println(scores[i] + "");
            }
            outFile.close();
        }
        check = true; //make sure we dont write to the file more than once.
    }


    //Draws all the text of the highscorer menu.
    public void drawContents(Graphics g, Font font, Font font2){
        g.setFont(font);
        for(int i = 0; i < names.length; i++){
            g.drawString((i + 1) + ":   " + names[i], 400, 300 + 60 * i);
            g.drawString(scores[i] + "", 900, 300 + 60 * i);
        }
        g.setFont(font2);
        g.drawString("Highscorers", 500, 200);
    }

    //draws the image and the contents for the highscorermenu.
    public void HighscoreDraw(Graphics g, Image background, Font font, Font font2) throws FileNotFoundException {
        g.setColor(Color.WHITE);
        g.drawImage(background,0,0, null);
        drawContents(g, font, font2);
    }
}

//Class for the boss.
class enemyShip{
    //x, y position of the enemy.
    private int x, y;
    //the angle the enemy is moving at.
    private int angle;
    //Status of if the enemy is shooting or not
    private int shotStatus;
    //Constants for if the enemy is shooting or not.
    public final int SHOOT = 1;
    public final int NOTSHOOT = 0;
    //Status for if the enemy is alive.
    private boolean aliveStatus;
    private int timer; //timer for when the enemy changes movement angle.

    public enemyShip(){
        x = 50;
        y = 50;
        angle = AsteroidsPane.randint(0,360);
        aliveStatus = false;
        shotStatus = NOTSHOOT;
        timer = -70;
    }

    //Gets the polygon of the enemy and returns it.
    public Polygon getEnemy(){
        int [] px = new int[11];
        int [] py = new int[11];
        int [][] points = new int[2][11];

        Point p1;
        int x2 = x+25;
        px[0] = x2;
        py[0] = y;

        px[1] = x-25;
        py[1] = y;

        p1 = xy(135, 12,x-25,y);

        px[2] = p1.x;
        py[2] = p1.y;

        px[3] = (int)(p1.x + 8.4852813* 2 + 50);
        py[3] = p1.y;

        px[4] = x + 25;
        py[4] = y;



        px[5] = px[3];
        py[5] = py[3];

        Point p2 = xy(135, 15, px[3] ,py[3]);

        px[6] = p2.x;
        py[6] = p2.y;

        int x = (int)(p2.x - (8.4852813* 2 + 50) + 10.60660172*2);
        px[7] = x;
        py[7] = p2.y;

        px[8] = px[2];
        py[8] = py[2];

        px[9] = px[1];
        py[9] = py[1];

        px[10] = px[0];
        py[10] = py[0];


        points[0] = px;
        points[1] = py;

        return new Polygon(points[0], points[1], 10);
    }


    public void setenemyStatus(boolean set){
        aliveStatus = set;
    }
    public boolean getenemyStatus(){
        return aliveStatus;
    }

    private Point xy(double ang, double mag, int x , int y){
        int px = (int)(x + Math.cos(Math.toRadians(ang)) * mag);
        int py = (int)(y - Math.sin(Math.toRadians(ang))*mag);
        return new Point(px,py);
    }

    //Draws the enemy by checking if the status of the enemy is alive.
    public void drawEnemy(Graphics g){
        if(aliveStatus == true){
            g.setColor(Color.WHITE);
            g.drawPolygon(getEnemy());
        }
    }

    //Moves the enemy using a change in x and y and uses the same logic to move the asteroids and bullets.
    public void enemyAdvance(double dist) {
        int changeX = 0;
        int changeY = 0;
        if(aliveStatus == true) {
            if (x < 1505 && x > -5 && y < 805 && y > -5) {
                changeX += Math.cos(Math.toRadians(angle)) * dist;
                changeY -= Math.sin(Math.toRadians(angle)) * dist;
                x += changeX;
                y -= changeY;
            }
            if (x > 1500) {
                x = 0;

            } else if (x < 0) {
                x = 1500;
            } else if (y > 800) {
                y = 0;
            } else if (y < 0) {
                y = 800;
            }
        }
    }

    //Changes the angle according to a timer and if the enemy is alive.
    public void changeAngle(){
        if(aliveStatus == true) {
            if (timer == 40) {
                angle = AsteroidsPane.randint(0, 360);
                timer = -70;
            }
            count();
        }
    }

    //counts down the timer.
    private void count(){
        if (timer < 40) {
            timer += 1;
        }

    }

    //Method is for shooting a bullet from the enemy. The method takes in an arraylist of bullets, and the hero.
    //It shoots towards the hero by getting the angle towards the hero by getting the archtangent of the change in
    //the y points and the x points. Then adds the bullet using that angle and the x and y position of the enemy.
    public ArrayList<Bullet> shoot(ArrayList<Bullet> bullets, Hero hero){
        if(aliveStatus == true){
            int angle = 0;
            //sets the shooting status to true every so often.
            if(AsteroidsPane.randint(1,100) == 1){
                shotStatus = SHOOT;
            }
            if(shotStatus == SHOOT){
                try{
                    angle = (int)Math.toDegrees(- Math.atan2(hero.getY()-y, hero.getX()-x));

                }catch(ArithmeticException e){
                }
                bullets.add(new Bullet(x,y, angle));
                shotStatus = NOTSHOOT;
            }
        }
        return bullets;
    }
}