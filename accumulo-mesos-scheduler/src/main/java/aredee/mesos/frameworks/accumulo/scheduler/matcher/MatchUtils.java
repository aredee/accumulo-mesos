package aredee.mesos.frameworks.accumulo.scheduler.matcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Static helper methods for handling lists of Match objects
 */
public class MatchUtils {
    // Don't allow instantiation
    private MatchUtils(){}

    /**
     * Iterates through list to find Match objects with no corresponding Offer
     *
     * @param matches List to check for Matches without Offers
     * @return List of Match objects with no Offer set
     */
    public static List<Match> getUnmatchedServers(List<Match> matches){
        List<Match> noMatch = new ArrayList<>();
        for( Match match: matches){
            if( !match.hasOffer() ){
                noMatch.add(match);
            }
        }
        return noMatch;
    }
}
