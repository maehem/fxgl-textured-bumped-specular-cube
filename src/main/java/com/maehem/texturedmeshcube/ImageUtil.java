/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maehem.texturedmeshcube;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Convert height map to a normal map.
 * Lifted from C++ example at Stack Overflow. (No license information was implied):
 * https://stackoverflow.com/questions/10652797/whats-the-logic-behind-creating-a-normal-map-from-a-texture
 * 
 * 
 * @author mark
 */
public class ImageUtil {
    
    /**
     * Height map should be a greyscale image. We will only read the green
     * channel for the brightness value.
     * 
     * @param heightMap
     * @return a normal map
     */
    public static Image heightToNormal( Image heightMap ) {
        int w = (int) heightMap.getWidth() - 1;
        int h = (int) heightMap.getHeight() - 1;
        
        WritableImage out = new WritableImage(
                (int)heightMap.getWidth(),(int)heightMap.getHeight()
        );
        PixelReader pr = heightMap.getPixelReader();
        PixelWriter pw = out.getPixelWriter();
        
        double sampleL;
        double sampleR;
        double sampleU;
        double sampleD;
        double xVector;
        double yVector;
        
        for (int y = 0; y < h + 1; y++) {
            for (int x = 0; x < w + 1; x++) {
                if (x > 0) { sampleL = pr.getColor(x-1, y).getGreen(); }
                else { sampleL = pr.getColor(x, y).getGreen(); }
                if (x < w) { sampleR = pr.getColor(x + 1, y).getGreen(); }
                else { sampleR = pr.getColor(x, y).getGreen(); }
                if (y > 1) { sampleU = pr.getColor(x, y - 1).getGreen(); }
                else { sampleU = pr.getColor(x, y).getGreen(); }
                if (y < h) { sampleD = pr.getColor(x, y + 1).getGreen(); }
                else { sampleD = pr.getColor(x, y).getGreen(); }
                xVector = (((sampleL - sampleR) + 1) * 0.5);
                yVector = (((sampleU - sampleD) + 1) * 0.5);
                Color col = new Color(1.0, xVector, yVector, 1.0);
                pw.setColor(x, y, col);
            }
        }
        
        return out;
    }
}
/*
namespace heightmap.Class
{
    class Normal
    {
        public void calculate(Bitmap image, PictureBox pic_normal)
        {
            Bitmap image = (Bitmap) Bitmap.FromFile(@"yourpath/yourimage.jpg");
            #region Global Variables
            int w = image.Width - 1;
            int h = image.Height - 1;
            float sample_l;
            float sample_r;
            float sample_u;
            float sample_d;
            float x_vector;
            float y_vector;
            Bitmap normal = new Bitmap(image.Width, image.Height);
            #endregion
            for (int y = 0; y < w + 1; y++)
            {
                for (int x = 0; x < h + 1; x++)
                {
                    if (x > 0) { sample_l = image.GetPixel(x - 1, y).GetBrightness(); }
                    else { sample_l = image.GetPixel(x, y).GetBrightness(); }
                    if (x < w) { sample_r = image.GetPixel(x + 1, y).GetBrightness(); }
                    else { sample_r = image.GetPixel(x, y).GetBrightness(); }
                    if (y > 1) { sample_u = image.GetPixel(x, y - 1).GetBrightness(); }
                    else { sample_u = image.GetPixel(x, y).GetBrightness(); }
                    if (y < h) { sample_d = image.GetPixel(x, y + 1).GetBrightness(); }
                    else { sample_d = image.GetPixel(x, y).GetBrightness(); }
                    x_vector = (((sample_l - sample_r) + 1) * .5f) * 255;
                    y_vector = (((sample_u - sample_d) + 1) * .5f) * 255;
                    Color col = Color.FromArgb(255, (int)x_vector, (int)y_vector, 255);
                    normal.SetPixel(x, y, col);
                }
            }
            pic_normal.Image = normal; // set as PictureBox image
        }
    }
}
*/