package hello.productos.identicons;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.WritableRaster;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class IdenticonGenerator {
    public static int height = 5;
    public static int width = 5;

    public static BufferedImage generate(String userName, HashGeneratorInterface hashGenerator) {
        byte[] hash = hashGenerator.generate(userName);

        BufferedImage identicon = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        WritableRaster raster = identicon.getRaster();

        //get byte values as unsigned ints
        int r = hash[0] & 255;
        int g = hash[1] & 255;
        int b = hash[2] & 255;

        int [] background = new int [] {255,255,255, 0};
        int [] foreground = new int [] {r, g, b, 255};

        for(int x=0 ; x < width ; x++) {

            int i = x < 3 ? x : 4 - x;
            for(int y=0 ; y < height; y++) {
                int [] pixelColor;

                if((hash[i] >> y & 1) == 1)
                    pixelColor = foreground;
                else
                    pixelColor = background;

                raster.setPixel(x, y, pixelColor);
            }
        }

        return identicon;
    }


    public byte[] generateIdenticonBytes(String hash) throws IOException {

        HashGeneratorInterface hashGenerator = new MessageDigestHashGenerator("MD5");

        BufferedImage identicon = IdenticonGenerator.generate(hash, hashGenerator);
        identicon = getScaledImage(identicon, 400, 400);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write( identicon, "png", baos );
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();

        return imageInByte;
    }

    /**
     * Resizes an image using a Graphics2D object backed by a BufferedImage.
     * @param src - source image to scale
     * @param w - desired width
     * @param h - desired height
     * @return - the new resized image
     */
    private BufferedImage getScaledImage(BufferedImage src, int w, int h){
        int finalw = w;
        int finalh = h;
        double factor = 1.0d;
        if(src.getWidth() > src.getHeight()){
            factor = ((double)src.getHeight()/(double)src.getWidth());
            finalh = (int)(finalw * factor);
        }else{
            factor = ((double)src.getWidth()/(double)src.getHeight());
            finalw = (int)(finalh * factor);
        }

        BufferedImage resizedImg = new BufferedImage(finalw, finalh, BufferedImage.TRANSLUCENT);
        Graphics2D g2 = resizedImg.createGraphics();
        //g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(src, 0, 0, finalw, finalh, null);
        g2.dispose();
        return resizedImg;
    }
}