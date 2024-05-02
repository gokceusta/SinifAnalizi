package sinifAnalizi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;


public class sinifAnalizi {

	public static void main(String[] args) {
	
		Scanner scanner = new Scanner(System.in);
		
        // Kullanıcıdan GitHub depo URLsi alma
        System.out.println("GitHub repository URL linkini giriniz:");
        String githubRepoUrl = scanner.nextLine();

        // Deponun klonlanacağı dosyayı oluşturma
        String localDirectory = System.getProperty("user.home") + File.separator + UUID.randomUUID().toString();

        // GitHub deposunu klonlama
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("git", "clone", githubRepoUrl, localDirectory);
            Process process = builder.start();
            int exitCode = process.waitFor();

            //İşlemin başarılı-başarısız sonuçlanmasına göre çıktı verme
            if (exitCode == 0) {
                System.out.println("Klonlanan dosya konumu:"+ localDirectory);
            } else {
                System.err.println("Klonlama hatası");
                extracted();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            extracted();
        }
        scanner.close(); // Scanner nesnesini kapatma
        
        String yerelDizin = localDirectory; // Klonlanan depo konum tanımı
        
        //*.java dosyalarını bulma
        List<File> javaDosyaları = javaUzanti(new File(yerelDizin));
        
        // Bulunan Java dosyalarını ve içeriğini yazdırma
        System.out.println("\nKlonlanan depodaki Java dosyaları:\n");
        for (File dosya : javaDosyaları) {
            if (sinifBulma(dosya)) {
                System.out.println("Sınıf: " + dosya.getName());
                int javadocSayisi = javadocHesapla(dosya);
                System.out.println("  ->Javadoc Satır Sayısı: " + javadocSayisi);
                int yorumSatirSayisi = yorumSatiri(dosya);
                System.out.println("  ->Yorum Satır Sayısı: " + yorumSatirSayisi);
                int kodSatirSayisi = kodSatirSayisi(dosya);
                System.out.println("  ->Kod Satır Sayısı: " + kodSatirSayisi);
                int herseyDahilKodSatirSayisi = LOC(dosya);
                System.out.println("  ->LOC: " + herseyDahilKodSatirSayisi);
                int fonksiyonSayisi = fonksiyonSayisi(dosya);
                System.out.println("  ->Fonksiyon Sayısı: " + fonksiyonSayisi);
                
                double YG = ((javadocSayisi + yorumSatirSayisi) * 0.8) / fonksiyonSayisi;
                double YH = (kodSatirSayisi / fonksiyonSayisi) * 0.3;
                double yorumSapmaYuzdesi = ((100 * YG) / YH) - 100;
                System.out.println("  ->Yorum Sapma Yüzdesi: %" + yorumSapmaYuzdesi);
                System.out.print("\n------------------------------\n");
            }
        }
    }
	
	//*.java dosyalarını bulma işlemi
    private static List<File> javaUzanti(File dizin) {
        List<File> javaDosyaları = new ArrayList<>();
        File[] dosyalar = dizin.listFiles();
        if (dosyalar != null) {
            for (File dosya : dosyalar) {
                if (dosya.isDirectory()) {
                    javaDosyaları.addAll(javaUzanti(dosya)); // Alt dizinlerdeki dosyaları arama
                } else if (dosya.isFile() && dosya.getName().endsWith(".java")) {
                    javaDosyaları.add(dosya); // *.java uzantılı dosyaları listeye ekleme
                }
            }
        }
        return javaDosyaları;
    }
    
    
    //Sınıf içerenleri bulma adımı
    private static boolean sinifBulma(File dosya) {
        try {
            String icerik = new String(Files.readAllBytes(Paths.get(dosya.getAbsolutePath())));
            return icerik.contains("class ");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    
    //Jaavadoc hesaplama kısmı
    private static int javadocHesapla(File dosya) {
        int javadocSatirSayisi = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(dosya))) {
            String satir;
            boolean javadocBasladi = false;
            while ((satir = br.readLine()) != null) {
                satir = satir.trim();
                if (satir.startsWith("*")) {
                    javadocBasladi = true;
                }
                if (javadocBasladi) {
                    javadocSatirSayisi++;
                }
                if (satir.endsWith("*/")) {
                    javadocBasladi = false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return javadocSatirSayisi;
    }
    
    
    //Yorum satırlarını hesaplama kodu
    private static int yorumSatiri(File dosya) {
        int yorumSatirSayisi = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(dosya))) {
            String satir;
            while ((satir = br.readLine()) != null) {
                if (satir.trim().startsWith("//") || satir.trim().startsWith("/*")) {
                    yorumSatirSayisi++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return yorumSatirSayisi;
    }
    
    
    //Kodların satır miktarını bulma adımı
    private static int kodSatirSayisi(File dosya) {
        int kodSatirSayisi = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(dosya))) {
            String satir;
            while ((satir = br.readLine()) != null) {
                satir = satir.trim();
                if (!satir.isEmpty() && !satir.startsWith("//") && !satir.startsWith("/*")) {
                    kodSatirSayisi++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return kodSatirSayisi;
    }
    
    
    //Tüm satırların sayısını bulma adımı
    private static int LOC(File dosya) {
        int herseyDahilKodSatirSayisi = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(dosya))) {
            while (br.readLine() != null) {
                herseyDahilKodSatirSayisi++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return herseyDahilKodSatirSayisi;
    }
    
    
    //Fonksiyon sayısını bulma adımı
    private static int fonksiyonSayisi(File dosya) {
        int fonksiyonSayisi = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(dosya))) {
            String satir;
            boolean icFonksiyon = false;
            while ((satir = br.readLine()) != null) {
                satir = satir.trim();
                if (satir.startsWith("public") || satir.startsWith("private") || satir.startsWith("protected")) {
                    if (satir.contains("(") && satir.contains(")") && satir.contains("{")) {
                        icFonksiyon = true;
                        fonksiyonSayisi++;
                    }
                } else if (icFonksiyon && satir.contains("}")) {
                    icFonksiyon = false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fonksiyonSayisi;
    }
    
    
	private static void extracted() {
	}
}
