package top.srcrs.util;

import java.util.Random;
import java.util.Arrays;
import java.util.List;

/**
 * 动态生成真实的 UserAgent
 * 支持桌面端和移动端，包含最新的浏览器版本
 * @author srcrs
 * @Time 2020-11-30
 */
public class InitUserAgent {
    
    private static final Random random = new Random();
    
    // 桌面端操作系统
    private static final List<String> DESKTOP_OS = Arrays.asList(
        "(Windows NT 10.0; Win64; x64)",
        "(Windows NT 10.0; WOW64)",
        "(Windows NT 11.0; Win64; x64)",
        "(Macintosh; Intel Mac OS X 10_15_7)",
        "(Macintosh; Intel Mac OS X 11_6_0)",
        "(Macintosh; Intel Mac OS X 12_0_1)",
        "(X11; Linux x86_64)",
        "(X11; Ubuntu; Linux x86_64)"
    );
    
    // 移动端操作系统
    private static final List<String> MOBILE_OS = Arrays.asList(
        "(iPhone; CPU iPhone OS 15_0 like Mac OS X)",
        "(iPhone; CPU iPhone OS 14_7_1 like Mac OS X)",
        "(iPad; CPU OS 15_0 like Mac OS X)",
        "(Linux; Android 11; SM-G991B)",
        "(Linux; Android 10; SM-A505FN)",
        "(Linux; Android 12; Pixel 6)"
    );
    
    // Chrome版本范围 (当前主流版本)
    private static final int[] CHROME_MAJOR_VERSIONS = {110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120};
    
    /**
     * 获取一个随机的桌面端 UserAgent
     * @return 桌面端 UserAgent
     */
    public static String getOne() {
        return getDesktopUserAgent();
    }
    
    /**
     * 获取桌面端 UserAgent
     * @return 桌面端 UserAgent
     */
    public static String getDesktopUserAgent() {
        String os = DESKTOP_OS.get(random.nextInt(DESKTOP_OS.size()));
        String chromeVersion = generateChromeVersion();
        
        return "Mozilla/5.0 " + os + " AppleWebKit/537.36 (KHTML, like Gecko) " + 
               chromeVersion + " Safari/537.36";
    }
    
    /**
     * 获取移动端 UserAgent
     * @return 移动端 UserAgent
     */
    public static String getMobileUserAgent() {
        String os = MOBILE_OS.get(random.nextInt(MOBILE_OS.size()));
        String chromeVersion = generateMobileChromeVersion();
        
        if (os.contains("iPhone") || os.contains("iPad")) {
            // iOS Safari
            return "Mozilla/5.0 " + os + " AppleWebKit/605.1.15 (KHTML, like Gecko) " +
                   "Version/15.0 Mobile/15E148 Safari/604.1";
        } else {
            // Android Chrome
            return "Mozilla/5.0 " + os + " AppleWebKit/537.36 (KHTML, like Gecko) " +
                   chromeVersion + " Mobile Safari/537.36";
        }
    }
    
    /**
     * 随机选择桌面端或移动端 UserAgent
     * @return 随机 UserAgent
     */
    public static String getRandomUserAgent() {
        // 70% 概率返回桌面端，30% 概率返回移动端
        return random.nextInt(100) < 70 ? getDesktopUserAgent() : getMobileUserAgent();
    }
    
    /**
     * 生成Chrome版本号
     * @return Chrome版本字符串
     */
    private static String generateChromeVersion() {
        int majorVersion = CHROME_MAJOR_VERSIONS[random.nextInt(CHROME_MAJOR_VERSIONS.length)];
        int minorVersion = random.nextInt(10);
        int buildVersion = random.nextInt(5000) + 1000;
        int patchVersion = random.nextInt(200);
        
        return "Chrome/" + majorVersion + "." + minorVersion + "." + buildVersion + "." + patchVersion;
    }
    
    /**
     * 生成移动端Chrome版本号
     * @return 移动端Chrome版本字符串
     */
    private static String generateMobileChromeVersion() {
        int majorVersion = CHROME_MAJOR_VERSIONS[random.nextInt(CHROME_MAJOR_VERSIONS.length)];
        int minorVersion = random.nextInt(10);
        int buildVersion = random.nextInt(5000) + 1000;
        int patchVersion = random.nextInt(200);
        
        return "Chrome/" + majorVersion + "." + minorVersion + "." + buildVersion + "." + patchVersion;
    }
    
    /**
     * 获取特定操作系统的 UserAgent
     * @param osType 操作系统类型 (windows, mac, linux, android, ios)
     * @return 指定操作系统的 UserAgent
     */
    public static String getUserAgentByOS(String osType) {
        switch (osType.toLowerCase()) {
            case "windows":
                String windowsOS = DESKTOP_OS.stream()
                    .filter(os -> os.contains("Windows"))
                    .skip(random.nextInt(3))
                    .findFirst()
                    .orElse(DESKTOP_OS.get(0));
                return "Mozilla/5.0 " + windowsOS + " AppleWebKit/537.36 (KHTML, like Gecko) " + 
                       generateChromeVersion() + " Safari/537.36";
            case "mac":
                String macOS = DESKTOP_OS.stream()
                    .filter(os -> os.contains("Macintosh"))
                    .skip(random.nextInt(3))
                    .findFirst()
                    .orElse(DESKTOP_OS.get(3));
                return "Mozilla/5.0 " + macOS + " AppleWebKit/537.36 (KHTML, like Gecko) " + 
                       generateChromeVersion() + " Safari/537.36";
            case "linux":
                String linuxOS = DESKTOP_OS.stream()
                    .filter(os -> os.contains("Linux"))
                    .skip(random.nextInt(2))
                    .findFirst()
                    .orElse(DESKTOP_OS.get(6));
                return "Mozilla/5.0 " + linuxOS + " AppleWebKit/537.36 (KHTML, like Gecko) " + 
                       generateChromeVersion() + " Safari/537.36";
            case "android":
                String androidOS = MOBILE_OS.stream()
                    .filter(os -> os.contains("Android"))
                    .skip(random.nextInt(3))
                    .findFirst()
                    .orElse(MOBILE_OS.get(3));
                return "Mozilla/5.0 " + androidOS + " AppleWebKit/537.36 (KHTML, like Gecko) " +
                       generateMobileChromeVersion() + " Mobile Safari/537.36";
            case "ios":
                String iosOS = MOBILE_OS.stream()
                    .filter(os -> os.contains("iPhone") || os.contains("iPad"))
                    .skip(random.nextInt(3))
                    .findFirst()
                    .orElse(MOBILE_OS.get(0));
                return "Mozilla/5.0 " + iosOS + " AppleWebKit/605.1.15 (KHTML, like Gecko) " +
                       "Version/15.0 Mobile/15E148 Safari/604.1";
            default:
                return getDesktopUserAgent();
        }
    }
}
