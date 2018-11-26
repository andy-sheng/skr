//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.utils;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.URLSpan;
import android.util.Patterns;
import android.webkit.WebView;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RongLinkify {
    public static final int WEB_URLS = 1;
    public static final int EMAIL_ADDRESSES = 2;
    public static final int PHONE_NUMBERS = 4;
    public static final int MAP_ADDRESSES = 8;
    public static final int ALL = 15;
    private static final int PHONE_NUMBER_MINIMUM_DIGITS = 5;
    private static final String WEB_URL_REGEX = "[-a-zA-Z0-9+&@#/%?=~_|!:.,;]*\\.[-a-zA-Z0-9+&@#%=~_|]{2,4}(:[0-9]{1,5})?(/[-a-zA-Z0-9+&@#/%?=~_|!:,;]*)*";
    public static final Pattern WEB_URL;
    private static final String PHONE_NUMBER_REGEX = "(\\d{2,}-\\d{5,})|(\\d{7,})";
    private static final Pattern PHONE_NUMBER;
    public static final android.text.util.Linkify.MatchFilter sUrlMatchFilter;
    public static final android.text.util.Linkify.MatchFilter sPhoneNumberMatchFilter;
    public static final android.text.util.Linkify.TransformFilter sPhoneNumberTransformFilter;

    public RongLinkify() {
    }

    public static final boolean addLinks(Spannable text, int mask) {
        if (mask == 0) {
            return false;
        } else {
            URLSpan[] old = (URLSpan[]) text.getSpans(0, text.length(), URLSpan.class);

            for (int i = old.length - 1; i >= 0; --i) {
                text.removeSpan(old[i]);
            }

            ArrayList<io.rong.imkit.utils.RongLinkify.LinkSpec> links = new ArrayList();
            if ((mask & 1) != 0) {
                gatherLinks(links, text, WEB_URL, new String[]{"http://", "https://", "rtsp://"}, sUrlMatchFilter, (android.text.util.Linkify.TransformFilter) null);
            }

            if ((mask & 2) != 0) {
                gatherLinks(links, text, Patterns.EMAIL_ADDRESS, new String[]{"mailto:"}, (android.text.util.Linkify.MatchFilter) null, (android.text.util.Linkify.TransformFilter) null);
            }

            if ((mask & 4) != 0) {
                gatherTelLinks(links, text, PHONE_NUMBER);
            }

            if ((mask & 8) != 0) {
                gatherMapLinks(links, text);
            }

            pruneOverlaps(links);
            if (links.size() == 0) {
                return false;
            } else {
                Iterator var4 = links.iterator();

                while (var4.hasNext()) {
                    io.rong.imkit.utils.RongLinkify.LinkSpec link = (io.rong.imkit.utils.RongLinkify.LinkSpec) var4.next();
                    applyLink(link.url, link.start, link.end, text);
                }

                return true;
            }
        }
    }

    public static final boolean addLinks(TextView text, int mask) {
        if (mask == 0) {
            return false;
        } else {
            CharSequence t = text.getText();
            if (t instanceof Spannable) {
                if (addLinks((Spannable) t, mask)) {
                    addLinkMovementMethod(text);
                    return true;
                } else {
                    return false;
                }
            } else {
                SpannableString s = SpannableString.valueOf(t);
                if (addLinks((Spannable) s, mask)) {
                    addLinkMovementMethod(text);
                    text.setText(s);
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    private static final void addLinkMovementMethod(TextView t) {
        MovementMethod m = t.getMovementMethod();
        if ((m == null || !(m instanceof LinkMovementMethod)) && t.getLinksClickable()) {
            t.setMovementMethod(LinkMovementMethod.getInstance());
        }

    }

    public static final void addLinks(TextView text, Pattern pattern, String scheme) {
        addLinks((TextView) text, pattern, scheme, (android.text.util.Linkify.MatchFilter) null, (android.text.util.Linkify.TransformFilter) null);
    }

    public static final void addLinks(TextView text, Pattern p, String scheme, android.text.util.Linkify.MatchFilter matchFilter, android.text.util.Linkify.TransformFilter transformFilter) {
        SpannableString s = SpannableString.valueOf(text.getText());
        if (addLinks((Spannable) s, p, scheme, matchFilter, transformFilter)) {
            text.setText(s);
            addLinkMovementMethod(text);
        }

    }

    public static final boolean addLinks(Spannable text, Pattern pattern, String scheme) {
        return addLinks((Spannable) text, pattern, scheme, (android.text.util.Linkify.MatchFilter) null, (android.text.util.Linkify.TransformFilter) null);
    }

    public static final boolean addLinks(Spannable s, Pattern p, String scheme, android.text.util.Linkify.MatchFilter matchFilter, android.text.util.Linkify.TransformFilter transformFilter) {
        boolean hasMatches = false;
        String prefix = scheme == null ? "" : scheme.toLowerCase(Locale.ROOT);
        Matcher m = p.matcher(s);

        while (m.find()) {
            int start = m.start();
            int end = m.end();
            boolean allowed = true;
            if (matchFilter != null) {
                allowed = matchFilter.acceptMatch(s, start, end);
            }

            if (allowed) {
                String url = makeUrl(m.group(0), new String[]{prefix}, m, transformFilter);
                applyLink(url, start, end, s);
                hasMatches = true;
            }
        }

        return hasMatches;
    }

    private static final void applyLink(String url, int start, int end, Spannable text) {
        URLSpan span = new URLSpan(url);
        text.setSpan(span, start, end, 33);
    }

    private static final String makeUrl(String url, String[] prefixes, Matcher m, android.text.util.Linkify.TransformFilter filter) {
        if (filter != null) {
            url = filter.transformUrl(m, url);
        }

        boolean hasPrefix = false;

        for (int i = 0; i < prefixes.length; ++i) {
            if (url.regionMatches(true, 0, prefixes[i], 0, prefixes[i].length())) {
                hasPrefix = true;
                if (!url.regionMatches(false, 0, prefixes[i], 0, prefixes[i].length())) {
                    url = prefixes[i] + url.substring(prefixes[i].length());
                }
                break;
            }
        }

        if (!hasPrefix) {
            url = prefixes[0] + url;
        }

        return url;
    }

    private static final void gatherLinks(ArrayList<io.rong.imkit.utils.RongLinkify.LinkSpec> links, Spannable s, Pattern pattern, String[] schemes, android.text.util.Linkify.MatchFilter matchFilter, android.text.util.Linkify.TransformFilter transformFilter) {
        Matcher m = pattern.matcher(s);

        while (true) {
            int start;
            int end;
            do {
                if (!m.find()) {
                    return;
                }

                start = m.start();
                end = m.end();
            } while (matchFilter != null && !matchFilter.acceptMatch(s, start, end));

            io.rong.imkit.utils.RongLinkify.LinkSpec spec = new io.rong.imkit.utils.RongLinkify.LinkSpec();
            String url = makeUrl(m.group(0), schemes, m, transformFilter);
            spec.url = url;
            spec.start = start;
            spec.end = end;
            links.add(spec);
        }
    }

    private static final void gatherTelLinks(ArrayList<io.rong.imkit.utils.RongLinkify.LinkSpec> links, Spannable s, Pattern pattern) {
        Matcher m = pattern.matcher(s);

        while (m.find()) {
            int start = m.start();
            int end = m.end();
            io.rong.imkit.utils.RongLinkify.LinkSpec spec = new io.rong.imkit.utils.RongLinkify.LinkSpec();
            spec.url = "tel:" + m.group();
            spec.start = start;
            spec.end = end;
            links.add(spec);
        }

    }

    private static final void gatherMapLinks(ArrayList<io.rong.imkit.utils.RongLinkify.LinkSpec> links, Spannable s) {
        String string = s.toString();
        int base = 0;

        try {
            String address;
            while ((address = WebView.findAddress(string)) != null) {
                int start = string.indexOf(address);
                if (start < 0) {
                    break;
                }

                io.rong.imkit.utils.RongLinkify.LinkSpec spec = new io.rong.imkit.utils.RongLinkify.LinkSpec();
                int length = address.length();
                int end = start + length;
                spec.start = base + start;
                spec.end = base + end;
                string = string.substring(end);
                base += end;
                String encodedAddress = null;

                try {
                    encodedAddress = URLEncoder.encode(address, "UTF-8");
                } catch (UnsupportedEncodingException var11) {
                    continue;
                }

                spec.url = "geo:0,0?q=" + encodedAddress;
                links.add(spec);
            }

        } catch (UnsupportedOperationException var12) {
            ;
        }
    }

    private static final void pruneOverlaps(ArrayList<io.rong.imkit.utils.RongLinkify.LinkSpec> links) {
        Comparator<io.rong.imkit.utils.RongLinkify.LinkSpec> c = new Comparator<io.rong.imkit.utils.RongLinkify.LinkSpec>() {
            public final int compare(io.rong.imkit.utils.RongLinkify.LinkSpec a, io.rong.imkit.utils.RongLinkify.LinkSpec b) {
                if (a.start < b.start) {
                    return -1;
                } else if (a.start > b.start) {
                    return 1;
                } else if (a.end < b.end) {
                    return 1;
                } else {
                    return a.end > b.end ? -1 : 0;
                }
            }
        };
        Collections.sort(links, c);
        int len = links.size();
        int i = 0;

        while (true) {
            while (i < len - 1) {
                io.rong.imkit.utils.RongLinkify.LinkSpec a = (io.rong.imkit.utils.RongLinkify.LinkSpec) links.get(i);
                io.rong.imkit.utils.RongLinkify.LinkSpec b = (io.rong.imkit.utils.RongLinkify.LinkSpec) links.get(i + 1);
                int remove = -1;
                if (a.start <= b.start && a.end > b.start) {
                    if (b.end <= a.end) {
                        remove = i + 1;
                    } else if (a.end - a.start > b.end - b.start) {
                        remove = i + 1;
                    } else if (a.end - a.start < b.end - b.start) {
                        remove = i;
                    }

                    if (remove != -1) {
                        links.remove(remove);
                        --len;
                        continue;
                    }
                }

                ++i;
            }

            return;
        }
    }

    static {
        WEB_URL = Patterns.WEB_URL;
        PHONE_NUMBER = Patterns.PHONE;
        sUrlMatchFilter = new android.text.util.Linkify.MatchFilter() {
            public final boolean acceptMatch(CharSequence s, int start, int end) {
                if (start == 0) {
                    return true;
                } else {
                    return s.charAt(start - 1) != '@';
                }
            }
        };
        sPhoneNumberMatchFilter = new android.text.util.Linkify.MatchFilter() {
            public final boolean acceptMatch(CharSequence s, int start, int end) {
                int digitCount = 0;

                for (int i = start; i < end; ++i) {
                    if (Character.isDigit(s.charAt(i))) {
                        ++digitCount;
                        if (digitCount >= 5) {
                            return true;
                        }
                    }
                }

                return false;
            }
        };
        sPhoneNumberTransformFilter = new android.text.util.Linkify.TransformFilter() {
            public final String transformUrl(Matcher match, String url) {
                return Patterns.digitsAndPlusOnly(match);
            }
        };
    }

    private static class LinkSpec {
        String url;
        int start;
        int end;

        private LinkSpec() {
        }
    }

    public interface TransformFilter {
        String transformUrl(Matcher var1, String var2);
    }

    public interface MatchFilter {
        boolean acceptMatch(CharSequence var1, int var2, int var3);
    }
}
