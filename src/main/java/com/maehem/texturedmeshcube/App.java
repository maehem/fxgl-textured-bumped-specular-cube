/**
 * Map a PNG image to a cube (triangle mesh) in JavaFX with FXGL
 * 
 * By Mark J Koch - 2021/10
 * 
 */
package com.maehem.texturedmeshcube;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.Camera3D;
import com.almasb.fxgl.dsl.EntityBuilder;
import com.almasb.fxgl.dsl.FXGL;
import static com.almasb.fxgl.dsl.FXGL.getGameScene;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.TransformComponent;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Point3D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

/**
 * App
 */
public class App extends GameApplication {

    private static final String DIRT_BLOCK_PNG = "/assets/textures/dirt_01.png";
    private static final String TEMPLATE_BLOCK_PNG = "cube-template.png";
    private static final String DEFAULT_BLOCK_PNG = DIRT_BLOCK_PNG;
    
    private Camera3D camera;
    private boolean titleSet = false;
    private TriangleMesh mesh;
    private Entity cube;
    private double viewRadius = 40;
    private double viewX = 0.0;
    private double viewY = 0.0;
    private int textureIndex = 0;
    ArrayList<Material> materials = new ArrayList<>();
    


    @Override
    protected void initSettings(GameSettings gs) {
        gs.setWidth(800);
        gs.setHeight(600);
        gs.set3D(true);
    }

    @Override
    protected void initGame() {
        // disable big mouse cursor
        getGameScene().getRoot().setCursor(Cursor.DEFAULT);
        // Set your custom cursor: (searched for in assets/ui/cursors/ )
        // getGameScene().setCursor(String imageName, Point2D hotspot);

        
        camera = getGameScene().getCamera3D();
        TransformComponent t = camera.getTransform();
        t.translateZ(-viewRadius);
        t.lookAt(new Point3D(0, 0, 0));
        
        buildTextureList();
        
        cube = new EntityBuilder()
                .at(0, 0, 0)
                .view(initMeshView())
                .buildAndAttach();
    }

    @Override
    protected void onUpdate(double tpf) {
        // JavaFX and FXGL version information not available until after game inits.
        if (!titleSet) {
            var javaVersion = System.getProperty("java.version");
            var javafxVersion = System.getProperty("javafx.version");
            var fxglVersion = FXGL.getVersion();

            String label = "FXGL Triangle Mesh Cube App :: Java: " + javaVersion + " with JavaFX: " + javafxVersion + " and FXGL: " + fxglVersion;
            FXGL.getPrimaryStage().setTitle(label);
            titleSet = true;
        }
        
        updateView();
    }

    @Override
    protected void initInput() {
        // Zoom in and out with mouse scroll wheel.
        getGameScene().getRoot().setOnScroll((t) -> {
            TransformComponent tr = camera.getTransform();
            viewRadius += t.getDeltaY()/10.0;
            if ( viewRadius < 20.0 ) viewRadius = 20.0;
            if ( viewRadius > 100.0) viewRadius = 100.0;
            System.out.println("View Radius: " + viewRadius);
        });
        // Rotate the camera around the cube by dragging mouse.
        getGameScene().getRoot().setOnMouseDragged((t) -> {
            TransformComponent tr = camera.getTransform();
            viewX = -t.getX()/50.0;
            viewY = (FXGL.getAppHeight()/2-t.getY())/4.0;
            if ( viewY < -50.0 ) viewY = -50.0;
            if ( viewY >  50.0 ) viewY =  50.0;
            
            //System.out.println("View X: " + viewX + "   Y: " + viewY);
        });
        Input input = FXGL.getInput();

        input.addAction(new UserAction("Previous Texture") {
            @Override
            protected void onActionBegin() {
                System.out.println("Previous Texture");
                textureIndex--;
                if ( textureIndex < 0 ) {
                    textureIndex = materials.size()-1; // Wrap around
                }
                fetchMeshViewFor(cube).setMaterial(materials.get(textureIndex));                
            }
        }, KeyCode.A);
        input.addAction(new UserAction("Next Texture") {
            @Override
            protected void onActionBegin() {
                System.out.println("Next Texture");
                textureIndex++;
                if ( textureIndex > materials.size()-1 ) {
                    textureIndex = 0; // Wrap around
                }
                fetchMeshViewFor(cube).setMaterial(materials.get(textureIndex));
            }
        }, KeyCode.D);
    }

    private MeshView fetchMeshViewFor( Entity e ) {
                ListIterator<Node> list = e.getViewComponent().getChildren().listIterator();
                while ( list.hasNext() ) {
                    Node n = list.next();
                    if ( n instanceof MeshView) {
                        return (MeshView) n;                     
                    }
                }
                return null;
    }
    
    private void updateView() {
            double sin = Math.sin(viewX/Math.PI);
            double cos = Math.cos(viewX/Math.PI);
            //System.out.println("sinX: " + sin + "   cosX: " + cos);
            TransformComponent tr = camera.getTransform();
            tr.setX(sin*viewRadius);
            tr.setZ(-cos*viewRadius);
            tr.setY(viewY);
            //tr.lookAt(new Point3D(0, 0, 0));        
            tr.lookAt(Point3D.ZERO);        
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    private MeshView initMeshView() {
        // The following code for a simple 3D mesh mapped cube,  was developed 
        // based on this very informative slide stack starting at slide 24.
        // https://www.slideshare.net/jpt1122/con2221-weaver-exploringjavafx3d
        
        mesh = new TriangleMesh();
        
        mesh.getPoints().addAll(
                 10.0f,  10.0f,  10.0f, // Vertex 0
                 10.0f,  10.0f, -10.0f, // Vertex 1
                 10.0f, -10.0f,  10.0f, // Vertex 2
                 10.0f, -10.0f, -10.0f, // Vertex 3
                -10.0f,  10.0f,  10.0f, // Vertex 4
                -10.0f,  10.0f, -10.0f, // Vertex 5
                -10.0f, -10.0f,  10.0f, // Vertex 6
                -10.0f, -10.0f, -10.0f  // Vertex 7
        );
        mesh.getTexCoords().addAll(
                0.25f, 0.00f,
                0.50f, 0.00f,
                0.00f, 0.333f,
                0.25f, 0.333f,
                0.50f, 0.333f,
                0.75f, 0.333f,
                1.00f, 0.333f,
                0.00f, 0.666f,
                0.25f, 0.666f,
                0.50f, 0.666f,
                0.75f, 0.666f,
                1.00f, 0.666f,
                0.25f, 1.00f,
                0.50f, 1.00f
        );
        
        // Faces List
        //
        // Example:   0,10 
        //            0 = index into points list.  
        //           10 = index into textCoords list.
        mesh.getFaces().addAll(
            0,10,  2,5,   1,9,
            2,5,   3,4,   1,9,
            4,7,   5,8,   6,2,
            6,2,   5,8,   7,3,
            0,13,  1,9,  4,12,
            4,12,  1,9,   5,8,
            2,1,   6,0,   3,4,
            3,4,   6,0,   7,3,
            0,10, 4,11,   2,5,
            2,5,  4,11,   6,6,
            1,9,   3,4,   5,8,
            5,8,   3,4,   7,3            
        );
        
        //Image im = new Image(getClass().getResourceAsStream(DEFAULT_BLOCK_PNG));
        //Material mat = new PhongMaterial(Color.GRAY, im, null, null, im);
        MeshView meshView = new MeshView(mesh);
        meshView.setMaterial(materials.get(0));
        
        return meshView;
    }
    
    private void setMesh(int index) {
    }
    
    private void buildTextureList() {
        File texDir;
        try {
            texDir = new File(getClass().getResource("/assets/textures").toURI());
            if ( texDir.isDirectory() ) {
                //System.out.println("Found textures.");
                File[] lumiFiles = texDir.listFiles((dir, name) -> name.endsWith("-difu.png"));
                for( File f: lumiFiles ) {
                    //System.out.println("    " + f.getName());
                    String bName = f.getName();
                    bName = bName.substring(0, bName.lastIndexOf('-'));
                    System.out.println("Base Name: " + bName);
                    Image im = new Image(new FileInputStream(f),256, 192, true, false);
                    Image bump = null;
                    InputStream bumpIs = getClass().getResourceAsStream("/assets/textures/" + bName + "-bump.png");
                    if ( bumpIs != null ) bump = new Image(bumpIs);
                    Image spec = null;
                    InputStream specIs = getClass().getResourceAsStream("/assets/textures/" + bName + "-spec.png");
                    if ( specIs != null ) spec = new Image(specIs);
                    Image lumi = null;
                    InputStream lumiIs = getClass().getResourceAsStream("/assets/textures/" + bName + "-lumi.png");
                    if ( lumiIs != null ) lumi = new Image(lumiIs);
                    Image normalMap = null;
                    if ( bump != null ) normalMap = ImageUtil.heightToNormal(bump);
                    Material mat = new PhongMaterial(Color.GRAY, im, spec, normalMap, lumi);
                    if ( f.getName().equals(TEMPLATE_BLOCK_PNG)) {
                        materials.add(textureIndex, mat);
                    } else {
                        materials.add(mat);
                    }
                }
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
