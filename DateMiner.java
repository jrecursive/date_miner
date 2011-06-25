// -------------------------------------------------------------------
//
// DateMiner
//
// Copyright (c) 2007-2010 John Muellerleile All Rights Reserved.
// jmuellerleile@gmail.com, @jrecursive
//
// This file is provided to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file
// except in compliance with the License.  You may obtain
// a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//
// -------------------------------------------------------------------

/***************************************/
/*                                     */
/* abandon all hope, ye who enter here */
/*                                     */
/***************************************/
                
import java.security.MessageDigest;
import java.util.*;
import java.io.*;
import java.lang.*;
import java.text.*;
import java.net.*;

import javax.swing.text.html.parser.ParserDelegator;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.MutableAttributeSet;

public class DateMiner {
    final private static String months_short[] = { "jan", "feb", "mar", "apr", 
        "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec" };
    final private static String months_long[] = { "january", "february", 
        "march", "april", "may", "june", "july", "august", "september", 
        "october", "november", "december" };

    // trace: set to true to see extraction "explain" / processing log
    private boolean trace = false;
    
	private static ArrayList<SimpleDateFormat> dateFormats = null;
	private static Calendar cal = Calendar.getInstance();
	
	static {
        // order does matter :)  it will match
        //  dates of the formats in the order you add them
        //
        
        dateFormats = new ArrayList<SimpleDateFormat>();
        dateFormats.add(new SimpleDateFormat("yyyy-MM-dd"));
        dateFormats.add(new SimpleDateFormat("yyyy MM dd"));
        dateFormats.add(new SimpleDateFormat("yyyy.MM.dd"));
        dateFormats.add(new SimpleDateFormat("yyyy-MMM-dd"));
        dateFormats.add(new SimpleDateFormat("yyyy MMM dd"));
        dateFormats.add(new SimpleDateFormat("yyyy.MMM.dd"));
        
        /*
        dateFormats.add(new SimpleDateFormat("dd-MM-yyyy"));
        dateFormats.add(new SimpleDateFormat("dd MM yyyy"));
        dateFormats.add(new SimpleDateFormat("dd.MM.yyyy"));
        dateFormats.add(new SimpleDateFormat("dd-MMM-yy"));
        dateFormats.add(new SimpleDateFormat("dd MMM yy"));
        dateFormats.add(new SimpleDateFormat("dd.MMM.yy"));
        dateFormats.add(new SimpleDateFormat("dd/MMM/yy"));
        */
        
        dateFormats.add(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"));
        dateFormats.add(new SimpleDateFormat("yyyy MM dd hh:mm:ss"));
        dateFormats.add(new SimpleDateFormat("yyyy.MM.dd hh:mm:ss"));
        dateFormats.add(new SimpleDateFormat("yyyy/MM/dd hh:mm:ss"));
        dateFormats.add(new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss"));
        dateFormats.add(new SimpleDateFormat("yyyy MMM dd hh:mm:ss"));
        dateFormats.add(new SimpleDateFormat("yyyy.MMM.dd hh:mm:ss"));
        dateFormats.add(new SimpleDateFormat("yyyy/MMM/dd hh:mm:ss"));
        
        /*
        dateFormats.add(new SimpleDateFormat("dd-MM-yyyy hh:mm:ss"));
        dateFormats.add(new SimpleDateFormat("dd MM yyyy hh:mm:ss"));
        dateFormats.add(new SimpleDateFormat("dd.MM.yyyy hh:mm:ss"));
        dateFormats.add(new SimpleDateFormat("dd/MM/yyyy hh:mm:ss"));
        dateFormats.add(new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss"));
        dateFormats.add(new SimpleDateFormat("dd MMM yyyy hh:mm:ss"));
        dateFormats.add(new SimpleDateFormat("dd.MMM.yyyy hh:mm:ss"));
        */
        
        dateFormats.add(new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss zzz"));
        dateFormats.add(new SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy"));
        dateFormats.add(new SimpleDateFormat("EEE, dd MMM yyyy zzz"));
        dateFormats.add(new SimpleDateFormat("EEE, MMM dd, yyyy"));
        dateFormats.add(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss zzz"));
        dateFormats.add(new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss zzz"));
        dateFormats.add(new SimpleDateFormat("MMM dd, yyyy"));
        dateFormats.add(new SimpleDateFormat("yyyy-MM-dd"));
        dateFormats.add(new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a"));
        dateFormats.add(new SimpleDateFormat("MM-dd-yyyy"));
        dateFormats.add(new SimpleDateFormat("MM.dd.yyyy"));
	}
    
    /* date finding / parsing */
    
    @SuppressWarnings("deprecation")
	public static ArrayList<Long> parseDate(String date) {
        date = date.trim();
        ArrayList<Long> pdates = new ArrayList<Long>();
        Date dt = null;
        
        for (SimpleDateFormat fmt : dateFormats) {
        	try {
        	    fmt.setLenient(true);
        		dt = fmt.parse(date);
        		if (hasYear(dt.toGMTString())) {
            		pdates.add(dt.getTime());
                }
        	}
            catch (Exception e) {
        		// par for the course
            }
        }
        try {
            SimpleDateFormat df = new SimpleDateFormat();
            df.setLenient(true);
            dt = df.parse(date);
            if (hasYear(dt.toGMTString())) {
                pdates.add(dt.getTime());
            }
        } catch (ParseException p_ex) {
            // par for the course
        }
        return pdates;
    }
    
    private static boolean hasYear(String gmt_dt) {
        int year = cal.get(Calendar.YEAR);
        boolean isdate = false;
        String[] dt_pts = gmt_dt.split(" ");
        for(String dt_pt : dt_pts) {
            if (dt_pt.length()<4) continue;
            try {
                int yr = Integer.parseInt(dt_pt);
                if (yr<=year && yr>1900)
                    isdate=true;
            } catch (Exception ex) {
                // par for the course
            }
        }
        return isdate;
    }
    
    public ArrayList<Long> findDatesInText(String u) throws Exception {
        ArrayList<Long> ar = new ArrayList<Long>();
        int i,j,k;
        Calendar cal = Calendar.getInstance();
        int cal_day = cal.get(Calendar.DATE);
        int cal_month = cal.get(Calendar.MONTH);
        int cal_year = cal.get(Calendar.YEAR);
        int cal_dow = cal.get(Calendar.DAY_OF_WEEK);
        int cal_dom = cal.get(Calendar.DAY_OF_MONTH);
        int cal_doy = cal.get(Calendar.DAY_OF_YEAR);
        Calendar rcal = null;

        String[] u_chunks = u.split(" ");
        
        /* 
         * sm1:
         *  found a possible year, [month, day || day, month]
         *   in direct succession = found a possible date!
        */
        boolean found_year = false;
        boolean found_month = false;
        boolean found_day = false;
        int pos_year = 0;
        int pos_month = 0;
        int pos_day = 0;
        
        int ch_c=0;
        int ch_sz=u_chunks.length;
        for(String u_chunk: u_chunks) {
            u_chunk = u_chunk.trim();
            if (u_chunk.equals("")) continue;
            dbg("chunk: " + u_chunk);
            boolean is_num = true;
            char uch[] = u_chunk.toCharArray();
            for(i=0; i<uch.length; i++) {
                char ch = uch[i];
                if (!Character.isDigit(ch)) {
                    is_num = false;
                    break;
                }
            }
            
            // text date hints, matching
            if (!is_num) {
                dbg("\tNaN, scanning for keywords (feb., EDT, etc.)");
                
                String ds = u_chunk.toLowerCase();
                int str_mo_num = 0;
                if (ds.length() == 3) {
                    // let's try month shorthand
                    for(String mo: months_short) {
                        if (ds.equals(mo)) {
                            pos_month = str_mo_num;
                            found_month = true;
                            dbg("\t\t!matched on month shorthand '" + 
                                mo +"', pos_month = " + pos_month);
                        }
                        str_mo_num++;
                    }
                } else if (ds.length()>3) {
                    for(String mo: months_long) {
                        if (ds.equals(mo)) {
                            pos_month = str_mo_num;
                            found_month = true;
                            dbg("\t\t!matched on long month name '" + 
                                mo +"', pos_month = " + pos_month);
                        }
                        str_mo_num++;
                    }
                } else {
                    dbg("\ti can't guess what '" + ds + "' is :(");
                    if (found_year) {
                        dbg("\t\t i found one (year, month) via sm1 " +
                            "(transition to !is_num):" +
                            " (" + pos_year + ", " +
                              pos_month + ", " + pos_day + ")");
                        rcal = Calendar.getInstance();
                        if (!found_day) {
                            pos_day = 1;
                        }
                        if (!found_month) {
                            pos_month = 0;
                        }
                        rcal.set(pos_year, pos_month-1, pos_day);
                        ar.add(rcal.getTimeInMillis());
                        dbg("\t\t**rcal = " + rcal.toString());
                    }
                    found_year = false;
                    found_month = false;
                    found_day = false;
                    pos_year = 0;
                    pos_month = 0;
                    pos_day = 0;
                }
            // numeric date hints, matching    
            } else {
                long tval=0;
                dbg("\tseems to be a number");
                if (u_chunk.length() == 12) {
                    dbg("\tlength is 12, trying 4/2/2/2/2, 2/2/4/2/2 combinations");
                    tval=brute_force_date(u_chunk,8); // todo: hh, mm (if we care)
                } else if (u_chunk.length() == 8) {
                    dbg("\tlength is 8, trying 4/2/2, 2/2/4 combinations");
                    tval=brute_force_date(u_chunk,8);
                } else if (u_chunk.length() == 6) {
                    dbg("\tlength is 6, trying 2/2/2, 4/2, 2/4 combinations");
                    // try last 2 digits as year (with brute force @ 8)
                    String s6_mmdd = u_chunk.substring(0,4);
                    String s6_yyyy;
                    if (Integer.parseInt( u_chunk.substring(4,6) ) < 70) {
                        s6_yyyy = "20" + u_chunk; // guess 20XX (we're good until 2069)
                    } else {
                        s6_yyyy = "19" + u_chunk;
                    }
                    String s6_u_chunk = s6_mmdd + s6_yyyy;
                    dbg("trying s6_u_chunk = " + s6_u_chunk);
                    tval=brute_force_date(s6_u_chunk,8);
                } else if (u_chunk.length() == 4) {
                    dbg("\tlength is 4, trying 4, 2/2 combinations");
                    int v1 = Integer.parseInt(u_chunk);
                    if (v1 > 1900 &&
                        v1 <= cal_year) {
                        pos_year = v1;
                        found_year = true;
                    } else {
                        if (!found_month &&
                            !found_day) {
                                v1 = Integer.parseInt(u_chunk.substring(0,2));
                                int v2 = Integer.parseInt(u_chunk.substring(2,4));
                                // assumes mm, dd first if either would match
                                if (v1 > 0 &&
                                    v1 <= 12 &&
                                    v2 > 0 &&
                                    v2 <= 31) {
                                        pos_month = v1;
                                        pos_day = v2;
                                        found_month = true;
                                        found_day = true;
                                } else if (
                                    v1 > 0 &&
                                    v1 <= 31 &&
                                    v2 > 0 &&
                                    v2 <= 12) {
                                        pos_month = v2;
                                        pos_day = v1;
                                        found_month = true;
                                        found_day = true;
                                }
                        } else {
                            dbg(u_chunk + " isn't a year, but we've either found_month or found_year (" +
                                found_month + ", " + found_year + ")");
                            
                            // we thought we found a month, but it turns out to
                            //  be something like this: /03/1114/ which usually
                            //  [at this state] turns out to be: 11/14/2003
                            if (found_month) {
                                String s4_2then4 = u_chunk + "20" + 
                                       (pos_month<10?"0":"") + 
                                       pos_month;
                                dbg("found what i thought was a month (" + pos_month + "), but now that " +
                                    "i've found '" + u_chunk + "', the month is probably a shorthand year " +
                                    "and this is the MMDD, giving that a whirl (if it's DDMM, there it's " +
                                    "possible brute_force_date(s,8) will detect it)");
                                tval = brute_force_date(s4_2then4,8);
                                found_month = false;
                                found_day = false;
                                found_year = false;
                                pos_year = 0;
                                pos_month = 0;
                                pos_day = 0;
                            }
                        }
                    }
                } else if (
                    u_chunk.length() == 2 ||
                    u_chunk.length() == 1) {
                    int orig_length = u_chunk.length();
                    
                    if (u_chunk.length() == 1) {
                        u_chunk = "0" + u_chunk;
                        dbg("transformed u_chunk into '" + u_chunk + "'");
                    }
                    dbg("\tlength is " + u_chunk.length() + 
                        ", trying to determine possibility of month or day");
                    int pos_v = Integer.parseInt(u_chunk);
                    
                    // if we have month and day already and it's length 2,
                    //  it's probably a shortened year... let's do the 2069 test
                    if (found_month &&
                        found_day &&
                        orig_length == 2) {
                        if (pos_v < 70) {
                            if ((2000 + pos_v) <= cal_year) {
                                pos_year = 2000 + pos_v;
                            } else {
                                dbg("\t\t\tfailed common sense test (@2069 test #2), " + 
                                    (2000 + pos_v) + " > " + cal_year + ", ignoring");
                            }
                        } else {
                            pos_year = 1900 + pos_v;
                        }
                        if (pos_year <= cal_year) {
                            found_year = true;
                            dbg("found_year by implication (already have month, day) " +
                                "via shorthand 2 digit year token");
                        } else {
                            pos_year = 0;
                            found_year = false;
                            dbg("unless we've found an e-time-machine, " + pos_year + 
                                " isn't a possible date, ignoring");
                        }
                        
                    // try parsing a month or day
                    // since we don't have both month and day found yet
                    // (reality may differ)
                    } else {
                        if (pos_v > 0 &&
                            pos_v <= 12) {
                                if (!found_month) {
                                    dbg("\t\t(is a month)");
                                    pos_month = pos_v;
                                    found_month = true;
                                } else {
                                    dbg("\t\t(is a day)");
                                    pos_day = pos_v;
                                    found_day = true;
                                }
                        } else if (
                            pos_v > 0 &&
                            pos_v <= 31 &&
                            !found_day) {
                                dbg("\t\t(is a day(case 2))");
                                pos_day = pos_v;
                                found_day = true;
                        } else {
                            dbg("\t\tdon't know what to do with pos_v = " + pos_v + " (" +
                                found_year + ", " + found_month + ", " + found_day + ")");
                            
                            // since at this point we've found either a month or a day, OR
                            //  the value presented at this state is not a candidate for what
                            //  CAN be found, IF we've got a year then let's add that as a date
                            //  (best guess), and augment with month if we've got that
                            if (found_year) {
                                String uc_s2_uk2 = "" + pos_year;
                                if (found_month) {
                                    uc_s2_uk2 += "" + (pos_month<10?"0":"") + pos_month;
                                } else {
                                    uc_s2_uk2 += "01"; // default month
                                }
                                uc_s2_uk2 += "01"; // default day
                                dbg("\t\tfound year (and possibly month), but current token is either not" +
                                    " a candidate for what we don't already have, so let's try cracking" +
                                    " this date open as '" + uc_s2_uk2 + "'");
                                tval=brute_force_date(uc_s2_uk2,8);
                            } else {
                                dbg("\t\tdon't have a year either yet, so looks like we're out of luck.");
                            }
                        }
                    }
                } else {
                    dbg("\tnon-obvious length " + u_chunk.length() + ", brute forcing combinations (trying 8)");
                    if (u_chunk.length()>=8) {
                        tval=brute_force_date(u_chunk,8);
                    } else {
                        dbg("\tdon't know what i can do with a number with u_chunk length " + u_chunk.length());
                    }
                }
                if (tval>0) {
                    dbg("\t\t** i found one: " + tval);
                    rcal = Calendar.getInstance();
                    rcal.setTimeInMillis(tval);
                    ar.add(rcal.getTimeInMillis());
                    dbg("\t\t**rcal = " + rcal.toString());
                    found_year = false;
                    found_month = false;
                    found_day = false;
                    pos_year = 0;
                    pos_month = 0;
                    pos_day = 0;
                    tval = 0;
                } else if (
                    found_year &&
                    found_month &&
                    found_day) {
                        dbg("\t\t i found one via sm1: (" + pos_year + ", " +
                            pos_month + ", " + pos_day + ")");
                        rcal = Calendar.getInstance();
                        rcal.set(pos_year, pos_month-1, pos_day);
                        dbg("\t\t**rcal = " + rcal.toString());
                        ar.add(rcal.getTimeInMillis());
                        found_year = false;
                        found_month = false;
                        found_day = false;
                        pos_year = 0;
                        pos_month = 0;
                        pos_day = 0;
                } else {
                    //dbg("\t[didn't find a date i can understand]");
                }
            }
            // last chance
            ch_c++;
            dbg("ch_c = " + ch_c + ", ch_sz = " + ch_sz);
            if (found_year &&
                found_month &&
                ch_c == ch_sz-1) {
                dbg("\t\t i found one (year, month) via sm1 (last u_chunk try): (" + 
                    pos_year + ", " + pos_month + ", " + pos_day + ")");
                rcal = Calendar.getInstance();
                if (!found_day) pos_day = 1;
                rcal.set(pos_year, pos_month-1, pos_day);
                ar.add(rcal.getTimeInMillis());
                dbg("\t\t**rcal = " + rcal.toString());
            }
        }
        if (found_year) {
            dbg("\t\t i found something via sm1 (last chance)! (" + pos_year + ", " +
                pos_month + ", " + pos_day + ")");
            rcal = Calendar.getInstance();
            if (!found_day) pos_day = 1;
            if (!found_month) pos_month = 0;
            rcal.set(pos_year, pos_month-1, pos_day);
            ar.add(rcal.getTimeInMillis());
            dbg("\t\t**rcal = " + rcal.toString());
        }
        // sanity check
        ArrayList<Long> ar2 = new ArrayList<Long>();
        for(long time_v: ar) {
            rcal.setTimeInMillis(time_v);
            if (rcal.get(Calendar.YEAR) < 1970 ||
                rcal.get(Calendar.YEAR) > cal_year) continue;
            ar2.add(time_v);
        }
        return ar2;
    }
    
    //
    // all "findDateIn[n](s)" methods return 0 on no sane date[/time] val found
    //
    public long brute_force_date(String s, int s_valid) throws Exception {
        Calendar cal = Calendar.getInstance();
        int cal_day = cal.get(Calendar.DATE);
        int cal_month = cal.get(Calendar.MONTH);
        int cal_year = cal.get(Calendar.YEAR);
        int cal_dow = cal.get(Calendar.DAY_OF_WEEK);
        int cal_dom = cal.get(Calendar.DAY_OF_MONTH);
        int cal_doy = cal.get(Calendar.DAY_OF_YEAR);
        
        Calendar rcal = null;
        
        /*
         * YYYY MM DD
        */
        int s1_1_4 = Integer.parseInt(s.substring(0,4));
        int s1_2_2 = Integer.parseInt(s.substring(4,6));
        int s1_3_2= Integer.parseInt(s.substring(6,8));
        
        int s1_4_2=0;
        int s1_5_2=0;
        if (s_valid == 12) {
            s1_4_2 = Integer.parseInt(s.substring(8,10)); // possible hh
            s1_5_2 = Integer.parseInt(s.substring(10,12)); // possible mm
        }
        
        /*
         * MM DD YYYY
         * DD MM YYYY
        */        
        int s2_1_2 = Integer.parseInt(s.substring(0,2));
        int s2_2_2 = Integer.parseInt(s.substring(2,4));
        int s2_3_4 = Integer.parseInt(s.substring(4,8));

        dbg("trying: ");
        dbg("in8; s1,1,4 = " + s1_1_4);
        dbg("in8; s1,2,2 = " + s1_2_2);
        dbg("in8; s1,3,2 = " + s1_3_2);
        
        
        // if   1900 < s1_1_4 < (current_year+1) AND
        //      0 < s1_2_2 < 13 AND
        //      0 < s1_3_2 < 32
        // then:
        //  year = s1_1_4
        //  month = s1_2_2
        //  day = s1_3_2
        //

        if (s1_1_4 > 1900 &&
            s1_1_4 < (cal_year+1) && // yy
            s1_2_2 > 0 &&
            s1_2_2 <= 12 && // mm
            s1_3_2 > 0 &&
            s1_3_2 <= 31) { // dd
                rcal = Calendar.getInstance();
                rcal.set(s1_1_4, s1_2_2-1, s1_3_2);
        
        // if   1900 < s2_3_4 < (current_year+1) AND
        //    (  0 < s2_1_2 < 13 AND
        //       0 < s2_2_2 < 32 ) 
        //      OR
        //    (  O < s2_1_2 < 32 AND
        //       0 < s2_2_2 < 13 )
        // then:
        //
        //  year = s2_3_4
        //  month = s2_1_2 / s2_2_2
        //  day = s2_2_2 / s2_1_2

        } else if (
            s2_3_4 > 1900 &&
            s2_3_4 < (cal_year+1)) {        
            // s2, s2_3_4 -> yy

            dbg("in8; s2,1,4 = " + s2_1_2);
            dbg("in8; s2,2,2 = " + s2_2_2);
            dbg("in8; s2,3,2 = " + s2_3_4);
            
            // dd mm yyyy?
            if (s2_1_2 > 0 &&
                s2_1_2 <= 31 &&
                s2_2_2 > 0 &&
                s2_2_2 <= 12) {
                    rcal = Calendar.getInstance();
                    rcal.set(s2_3_4, s2_2_2-1, s2_1_2); /* -1 => 0-based month */
                    
            // mm dd yyyy?
            } else if (s2_1_2 > 0 &&
                s2_1_2 <= 12 &&
                s2_2_2 > 0 &&
                s2_2_2 <= 31) {
                    rcal = Calendar.getInstance();
                    rcal.set(s2_3_4, s2_1_2-1, s2_2_2); /* -1 => 0-based month */
            }
        }
        // else: can't find aliens in this washing machine :(
        if (rcal == null) return 0;
        else return (rcal.getTimeInMillis());
    }
    
    //
    // tailored specifically to URLs, but not limited to :)
    //
    private ArrayList<Long> coerceDatesFromText(String u) throws Exception {
        int i,j,k; /* good sign, right? */
        final String collapseChars[] = { "\\/", "\\_", "\\-", "\\?", "\\.", "\\&", "\\=", "\\," };
        
        dbg("coerceDatesFromText(" + u + ")");
        if (u.toLowerCase().startsWith("http")) {
            dbg("* coerceDatesFromText: detected url (via http)");
            u = u.replace("http://", "");
            u = u.replace("https://", "");
            // everything after domain
            u = u.substring(u.indexOf("/"));
            dbg("after domain substring: " + u);
        }
        
        for(String c_ch: collapseChars) {
            u = u.replaceAll(c_ch, " ");
        }
        
        String new_u2 = "";
        String u2_chunks[] = u.split(" ");
        for(String u2s: u2_chunks) {
            u2s = u2s.trim().toLowerCase();
            boolean is_month_token = false;
            String detected_token = null;
            for(String mo: months_short) {
                if (u2s.equals(mo)) {
                    detected_token = mo;
                    is_month_token = true;
                }
            }
            if (is_month_token) {
                dbg("coerceDatesFromText: (strip/u2_chunks) " +
                    "keeping detected month token '" + 
                    detected_token + "'");
            } else {
                u2s = u2s.replaceAll("[A-Za-z]", " ");
                if (u2s.trim().equals("")) continue;
            }
            new_u2 += u2s + " ";
        }
        String u2 = new_u2.trim();

        dbg("after collapse: " + u);
        dbg("after strip:    " + u2);
        
        ArrayList<Long> ar = findDatesInText(u);
        ar.addAll(findDatesInText(u2));
        ArrayList<Long> ar_result = new ArrayList<Long>();
        long pval = 0;
        
        if (ar.size() > 0) {
            for(long tval: ar) {
                if (tval == pval) continue;
                pval = tval; /* haha, sorry again */
                Calendar rcal = Calendar.getInstance();
                rcal.setTimeInMillis(tval);
                dbg("[found date] " + 
                    (rcal.get(Calendar.MONTH)+1) + "/" +
                    rcal.get(Calendar.DAY_OF_MONTH) + "/" +
                    rcal.get(Calendar.YEAR));
                ar_result.add(tval);
            }
        }
        return ar_result;
    }
    
    private ArrayList<Long> coerceDatesFromURL(String url) throws Exception {
        String content;
        final ArrayList<Long> dates = new ArrayList<Long>();
                
        dbg("coerceDatesFromURL url = " + url);
        
        if (url.toLowerCase().startsWith("http")) {
            try {
                content = geturl(url);
                if (content == null) return new ArrayList<Long>();
            } catch (Exception websiteTimeout) {
                websiteTimeout.printStackTrace();
                return new ArrayList<Long>();
            }
        } else {
            content = url;
        }
        StringReader reader = new StringReader(content);
        
        ParserDelegator parserDelegator = new ParserDelegator();
        ParserCallback parserCallback = new ParserCallback() {
          boolean parsingDates = false;
          public void handleText(final char[] data, final int pos) {
            try {
                if (parsingDates) {
                    String pos_date_content = (new String(data)).trim();
                    dbg("handleText <" + pos + ">: data = " + pos_date_content);
                    ArrayList<Long> cdates = coerceDatesFromText(pos_date_content);
                    if (cdates.size() == 0) {
                        dbg("found no dates in: \n" + pos_date_content + "\n\n");
                    } else {
                        for(long tval:cdates) { dates.add(tval); }
                    }
                }
            } catch (Exception ex) {
                dbg("coerceDatesFromURL: handleText: " + ex);
                ex.printStackTrace();
            }
          }
          public void handleStartTag(Tag tag, MutableAttributeSet attribute, int pos) {
            for (Enumeration e = attribute.getAttributeNames(); e.hasMoreElements();) {
                Object attr = e.nextElement();
                String attr_nm = "" + attr;
                String attr_v = "" + attribute.getAttribute(attr);
                if ( (""+attr_v).toLowerCase().indexOf("date")!=-1 ||
                     (""+attr_v).toLowerCase().indexOf("_dt")!=-1 ||
                     (""+attr_v).toLowerCase().indexOf("-dt")!=-1) {
                        dbg("handleStartTag <" + pos + ">: tag = " + tag + 
                            ", attr_nm = " + attr_nm + " -> " + attr_v);
                        parsingDates = true;
                }
            }
          }
          public void handleEndTag(Tag tag, final int pos) {
            if (parsingDates) {
                dbg("handleEndTag <" + pos + ">: tag = " + tag + " (parsingDates/STOP)");
                parsingDates = false;
            }
          }
          public void handleSimpleTag(Tag tag, MutableAttributeSet a, final int pos) { }
          public void handleComment(final char[] data, final int pos) { }
          public void handleError(final java.lang.String errMsg, final int pos) { }
        };
        parserDelegator.parse(reader, parserCallback, true);
        return dates;
    }
    
    public long coerceDates(String url) throws Exception {
        dbg("extracting from url: " + url);
        ArrayList<Long> dates_url = coerceDatesFromText(url);
        dbg("scanning content for url: " + url);
        dbg("-- content dates --");
        ArrayList<Long> dates_content = coerceDatesFromURL(url);
        
        // date reconciliation
        ArrayList<String> explain = new ArrayList<String>();
        
        // if there are dates extracted from the url,
        //  and one or more of those exist in dates
        //  extracted from the content, consider them
        //  "most likely"
        ArrayList<Long> most_likely = new ArrayList<Long>();
        if (dates_url.size() > 0 && 
            dates_content.size() > 0) {
            for(long url_tval: dates_url) {
                for(long content_tval: dates_content) {
                    if (url_tval == content_tval) {
                        dbg("most_likely.add(" + url_tval + ")");
                        explain.add("most_likely date [" + url_tval + "], reason: " +
                            "date appears in both url and content");
                        most_likely.add(url_tval);
                    }
                }
            }
        }
        
        // if most_likely has 1 element, which means 1 date exactly has been
        //  found in both sets, that's our winner (no matter what);
        //  otherwise, let the "datestimating" begin . . . 
        if (most_likely.size() != 1) {
            // otherwise:
            //  if there are dates found in the url but not 
            //   in the content, they are most_likely
            if (dates_url.size() > 0 && 
                dates_content.size() == 0) {
                for(long url_tval: dates_url) {
                    most_likely.add(url_tval);
                    explain.add("most_likely date [" + url_tval + "], reason: " +
                        "date appears in url, no dates found in content");
                }
                
            // if there are dates found in the content, but not
            //  in the url, they are most_likely
            } else if(dates_content.size() > 0 && 
                dates_url.size() == 0) {
                for(long content_tval: dates_content) {
                    most_likely.add(content_tval);
                    explain.add("most_likely date [" + content_tval + "], reason: " +
                        "date appears in content, no dates found in url");
                }
            }
            
            // if no dates are most_likely at this point, add url
            //  dates first, then content dates; we'll trim the
            //  edges (if they exist) next
            if (most_likely.size() == 0) {
                explain.add("adding both url and content dates to most likely and " +
                    "relying on trimming outliers to find a reasonable date, reason: " +
                    "there are dates found in both url and content, but none are present " +
                    "in both sets.");
                for(long url_tval: dates_url) {
                    most_likely.add(url_tval);
                    explain.add("most_likely date [" + url_tval + "], reason: " +
                        "date appears in url, no dates found in content");
                }
                for(long content_tval: dates_content) {
                    most_likely.add(content_tval);
                    explain.add("most_likely date [" + content_tval + "], reason: " +
                        "date appears in content, no dates found in url");
                }
            }
        }
        
        // trim outliers
        // todo: trim outliers! :D
        ArrayList<Long> ld_ar = new ArrayList<Long>();
        for(long tval: most_likely) {
            if (!ld_ar.contains(tval)) ld_ar.add(tval);
        }
        
        if (ld_ar.size()>0) {
            
            if (trace) {
                dbg("------------ most_likely dates ------------");
                for(String expl_s: explain) dbg("> " + expl_s);
            }
                        
            long oldest_tval = -1, newest_tval = -1;
            for(long likely_date: ld_ar) {
                if (newest_tval == -1 ||
                    likely_date > newest_tval) {
                    newest_tval = likely_date;
                }
                if (oldest_tval == -1 ||
                    likely_date < oldest_tval) {
                    oldest_tval = likely_date;
                }
                dbg("likely_date = " + likely_date);
            }
            Calendar lcal = Calendar.getInstance();
            lcal.setTimeInMillis(newest_tval);
            dbg("[" + (ld_ar.size()==1?"only":"newest") + "] " +
                "most likely overall date: " + 
                (lcal.get(Calendar.MONTH)+1) + "/" +
                lcal.get(Calendar.DAY_OF_MONTH) + "/" +
                lcal.get(Calendar.YEAR) + "    " + url + 
                "    out of " + ld_ar.size() + " possible values");
            return lcal.getTimeInMillis();
        } else {
            dbg("**** no dates found !!");
            return 0;
        }
    }
    
    /* misc */
    
    public void setTrace(boolean tf) {
        this.trace = tf;
    }
    
    public static String trimJunk(String s) {
        return s.replaceAll("[^\\p{ASCII}]"," ").trim();
    }
    
    private String geturl(String u) throws Exception {
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");
        dbg("geturl(" + u + ")");
        String s;
        URL url = new URL(u);
        InputStream is = url.openStream();
        DataInputStream dis = new DataInputStream(new BufferedInputStream(is));
        StringBuffer sb = new StringBuffer();
        while ((s = dis.readLine()) != null) {
            sb.append(s);
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    // todo: java logging
    private void dbg(String s) { if (trace) System.out.println(s); }

    /* test */
    public static void main(String args[]) throws Exception {
        DateMiner tu = new DateMiner();
        tu.setTrace(true);
        String url;
        if (args.length == 0) {
            //url = "http://news.prnewswire.com/ViewContent.aspx?ACCT=109&STORY=/www/story/04-08-2009/0005002844&EDATE=";
            url = "http://www.cnn.com/2010/US/05/20/gulf.oil.spill/index.html?hpt=T2";
        } else {
            url = args[0];
        }
        long final_dt = tu.coerceDates(url);
        System.out.println("final resolved date: " + final_dt);
    }
}

