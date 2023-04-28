import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class HappyPoker {
    int[] res = null;
    static class Pokers {
        public Map<Integer, Integer> cards;
        public long state;
        public Pokers(int[] cards) {
            modify(cards);
            this.cards = new HashMap<>();
            this.state = 0;
            for (int card : cards) {
                add(card);
            }
        }
        
        public static void modify(int[] cards) {
            for (int i = 0; i < cards.length; i++) {
                if (cards[i] == 2) {
                    cards[i] = 16;
                }
            }
        }
        
        public void addState(int card) {
            if (card == 18) {
                this.state += Math.pow(5, 15);
            } else if (card == 17) {
                this.state += Math.pow(5, 14);
            } else if (card == 16) {
                this.state += 5;
            } else if (card == 14) {
                this.state += 1;
            } else {
                this.state += Math.pow(5, card - 1);
            }
        }

        public void removeState(int card) {
            if (card == 18) {
                this.state -= Math.pow(5, 15);
            } else if (card == 17) {
                this.state -= Math.pow(5, 14);
            } else if (card == 16) {
                this.state -= 5;
            } else if (card == 14) {
                this.state -= 1;
            } else {
                this.state -= Math.pow(5, card - 1);
            }
        }

        public void add(int card) {
            add(card, 1);
        }

        public void add(int card, int cnt) {
            for (int i = 0; i < cnt; i++) {
                cards.put(card, 1 + cards.getOrDefault(card, 0));
                addState(card);
            }
        }

        public void remove(int card) {
            remove(card, 1);
        }

        public void remove(int card, int cnt) {
            for (int i = 0; i < cnt; i++) {
                cards.put(card, cards.getOrDefault(card, 0) - 1);
                removeState(card);
            }
        }

        public List<Integer> keys() {
            return keys(1);
        }

        public List<Integer> keys(int minCnt) {
            return keys(minCnt, 3);
        }

        public List<Integer> keys(int minCnt, int limit) {
            List<Integer> res = new ArrayList<>();
            cards.forEach((key, value) -> {
                if (value >= minCnt && key >= limit) {
                    res.add(key);
                }
            });
            return res;
        }

        public void addStraight(int low, int high) {
            for (int card = low; card <= high; card++) {
                add(card);
            }
        }

        public void removeStraight(int low, int high) {
            for (int card = low; card <= high; card++) {
                remove(card);
            }
        }
    }

    boolean mp = true;

    Pokers comp = null;
    Pokers mine = null;

    Map<String, Boolean> dp = new HashMap<>();

    public boolean dfs(boolean myTurn, int[] p) {
        if (mp) {
            Pokers.modify(p);
            mp = false;
        }
        if (comp.state == 0L) {
            return false;
        }
        if (mine.state == 0L) {
            return true;
        }
        long pState = 0L;
        for (int card : p) {
            if (card == 18) {
                pState += Math.pow(5, 15);
            } else if (card == 17) {
                pState += Math.pow(5, 14);
            } else if (card == 16) {
                pState += 5;
            } else if (card == 14) {
                pState += 1;
            } else {
                pState += Math.pow(5, card - 1);
            }
        }

        StringBuilder key = new StringBuilder();
        key.append(mine.state)
                .append(",")
                .append(comp.state)
                .append(myTurn)
                .append(pState);
        String k = key.toString();
        key = null;
        if (dp.containsKey(k)) {
            return dp.get(k);
        }

        // Pass
        if (myTurn && p.length > 0 && dfs(false, new int[0])) {
            res = new int[]{0};
            dp.put(k, true);
            return true;
        }
        if (!myTurn && p.length > 0 && !dfs(true, new int[0])) {
            dp.put(k, false);
            return false;
        }

        // Single
        if (p.length == 0 || p.length == 1) {
            int limit = (p.length == 0) ? 3 : p[0] + 1;
            if (!myTurn) {
                for (int single : comp.keys(1, limit)) {
                    comp.remove(single);
                    if (!dfs(true, new int[]{single})) {
                        comp.add(single);
                        dp.put(k, false);
                        return false;
                    }
                    comp.add(single);
                }
            } else {
                for (int single : mine.keys(1, limit)) {
                    mine.remove(single);
                    if (dfs(false, new int[]{single})) {
                        mine.add(single);
                        dp.put(k, true);
                        res = new int[]{single};
                        return true;
                    }
                    mine.add(single);
                }
            }
        }

        // Pair
        if (!myTurn && (p.length == 0 || p.length == 2)) {
            int limit = p.length == 0 ? 3 : p[0] + 1;
            for (int pair : comp.keys(2, limit)) {
                comp.remove(pair, 2);
                if (!dfs(true, new int[]{pair, pair})) {
                    comp.add(pair, 2);
                    dp.put(k, false);
                    return false;
                }
                comp.add(pair, 2);
            }
        }

        if (myTurn && (p.length == 0 || p.length == 2)) {
            int limit = p.length == 0 ? 3 : p[0] + 1;
            for (int pair : mine.keys(2, limit)) {
                mine.remove(pair, 2);
                if (dfs(false, new int[]{pair, pair})) {
                    mine.add(pair, 2);
                    dp.put(k, true);
                    res = new int[]{pair, pair};
                    return true;
                }
                mine.add(pair, 2);
            }
        }

        // Triple
        if (!myTurn && (p.length == 0 || p.length == 3)) {
            int limit = p.length == 0 ? 3 : p[0] + 1;
            for (int triple : comp.keys(3, limit)) {
                comp.remove(triple, 3);
                if (!dfs(true, new int[]{triple, triple, triple})) {
                    comp.add(triple, 3);
                    dp.put(k, false);
                    return false;
                }
                comp.add(triple, 3);
            }
        }

        if (myTurn && (p.length == 0 || p.length == 3)) {
            int limit = p.length == 0 ? 3 : p[0] + 1;
            for (int triple : mine.keys(3, limit)) {
                mine.remove(triple, 3);
                if (dfs(false, new int[]{triple, triple, triple})) {
                    mine.add(triple, 3);
                    dp.put(k, true);
                    res = new int[]{triple, triple, triple};
                    return true;
                }
                mine.add(triple, 3);
            }
        }

        // Three one
        if (!myTurn && (p.length == 0 || (p.length == 4 && !isBomb(p)))) {
            int limit = p.length == 0 ? 3 : p[0] + 1;
            for (int triple : comp.keys(3, limit)) {
                comp.remove(triple, 3);
                for (int single : comp.keys()) {
                    comp.remove(single);
                    if (!dfs(true, new int[]{triple, triple, triple, single})) {
                        comp.add(triple, 3);
                        comp.add(single);
                        dp.put(k, false);
                        return false;
                    }
                    comp.add(single);
                }
                comp.add(triple, 3);
            }
        }

        if (myTurn && (p.length == 0 || (p.length == 4 && !isBomb(p)))) {
            int limit = p.length == 0 ? 3 : p[0] + 1;
            for (int triple : mine.keys(3, limit)) {
                mine.remove(triple, 3);
                for (int single : mine.keys()) {
                    mine.remove(single);
                    if (dfs(false, new int[]{triple, triple, triple, single})) {
                        mine.add(triple, 3);
                        mine.add(single);
                        dp.put(k, true);
                        res = new int[]{triple, triple, triple, single};
                        return true;
                    }
                    mine.add(single);
                }
                mine.add(triple, 3);
            }
        }

        // Three pair
        if (!myTurn && (p.length == 0 || isFull(p))) {
            int limit = p.length == 0 ? 3 : p[0] + 1;
            for (int triple : comp.keys(3, limit)) {
                comp.remove(triple, 3);
                for (int pair : comp.keys(2)) {
                    comp.remove(pair, 2);
                    if (!dfs(true, new int[]{triple, triple, triple, pair, pair})) {
                        comp.add(triple, 3);
                        comp.add(pair, 2);
                        dp.put(k, false);
                        return false;
                    }
                    comp.add(pair, 2);
                }
                comp.add(triple, 3);
            }
        }

        if (myTurn && (p.length == 0 || isFull(p))) {
            int limit = p.length == 0 ? 3 : p[0] + 1;
            for (int triple : mine.keys(3, limit)) {
                mine.remove(triple, 3);
                for (int pair : mine.keys(2)) {
                    mine.remove(pair, 2);
                    if (dfs(false, new int[]{triple, triple, triple, pair, pair})) {
                        mine.add(triple, 3);
                        mine.add(pair, 2);
                        dp.put(k, true);
                        res = new int[]{triple, triple, triple, pair, pair};
                        return true;
                    }
                    mine.add(pair, 2);
                }
                mine.add(triple, 3);
            }
        }

        // Bomb
        if (!myTurn) {
            if (comp.cards.getOrDefault(18, 0) > 0
                    && comp.cards.getOrDefault(17, 0) > 0) {
                comp.remove(18);
                comp.remove(17);
                if (!dfs(true, new int[]{18, 17})) {
                    comp.add(18);
                    comp.add(17);
                    dp.put(k, false);
                    return false;
                }
                comp.add(18);
                comp.add(17);
            }
        } else {
            if (mine.cards.getOrDefault(18, 0) > 0
                    && mine.cards.getOrDefault(17, 0) > 0) {
                mine.remove(18);
                mine.remove(17);
                if (dfs(false, new int[]{18, 17})) {
                    mine.add(18);
                    mine.add(17);
                    dp.put(k, true);
                    res = new int[]{18, 17};
                    return true;
                }
                mine.add(18);
                mine.add(17);
            }
        }

        if (!myTurn) {
            int limit = p.length == 0 || !isBomb(p) ? 3 : p[0] + 1;
            for (int bomb : comp.keys(4, limit)) {
                comp.remove(bomb, 4);
                if (!dfs(true, new int[]{bomb, bomb, bomb, bomb})) {
                    comp.add(bomb, 4);
                    dp.put(k, false);
                    return false;
                }
                comp.add(bomb, 4);
            }
        }

        if (myTurn) {
            int limit = p.length == 0 || !isBomb(p) ? 3 : p[0] + 1;
            for (int bomb : mine.keys(4, limit)) {
                mine.remove(bomb, 4);
                if (dfs(false, new int[]{bomb, bomb, bomb, bomb})) {
                    mine.add(bomb, 4);
                    dp.put(k, true);
                    res = new int[]{bomb, bomb, bomb, bomb};
                    return true;
                }
                mine.add(bomb, 4);
            }
        }

        // Bomb with two singles
        if (!myTurn && (p.length == 0 || (p.length == 6 && withBomb(p)))) {
            int limit = p.length == 0 ? 3 : p[0] + 1;
            for (int bomb : comp.keys(4, limit)) {
                comp.remove(bomb, 4);
                for (int s1 : comp.keys()) {
                    comp.remove(s1);
                    for (int s2 : comp.keys()) {
                        comp.remove(s2);
                        if (!dfs(true, new int[]{bomb, bomb, bomb, bomb, s1, s2})) {
                            comp.add(bomb, 4);
                            comp.add(s1);
                            comp.add(s2);
                            dp.put(k, false);
                            return false;
                        }
                        comp.add(s2);
                    }
                    comp.add(s1);
                }
                comp.add(bomb, 4);
            }
        }

        if (myTurn && (p.length == 0 || (p.length == 6 && withBomb(p)))) {
            int limit = p.length == 0 ? 3 : p[0] + 1;
            for (int bomb : mine.keys(4, limit)) {
                mine.remove(bomb, 4);
                for (int s1 : mine.keys()) {
                    mine.remove(s1);
                    for (int s2 : mine.keys()) {
                        mine.remove(s2);
                        if (dfs(false, new int[]{bomb, bomb, bomb, bomb, s1, s2})) {
                            mine.add(bomb, 4);
                            mine.add(s1);
                            mine.add(s2);
                            dp.put(k, true);
                            res = new int[]{bomb, bomb, bomb, bomb, s1, s2};
                            return true;
                        }
                        mine.add(s2);
                    }
                    mine.add(s1);
                }
                mine.add(bomb, 4);
            }
        }

        // Bomb with two pairs
        if (!myTurn && (p.length == 0 || (p.length == 8 && withBomb(p)))) {
            int limit = p.length == 0 ? 3 : p[0] + 1;
            for (int bomb : comp.keys(4, limit)) {
                comp.remove(bomb, 4);
                for (int s1 : comp.keys(2)) {
                    comp.remove(s1, 2);
                    for (int s2 : comp.keys(2)) {
                        comp.remove(s2, 2);
                        if (!dfs(true, new int[]{bomb, bomb, bomb, bomb, s1, s1, s2, s2})) {
                            comp.add(bomb, 4);
                            comp.add(s1, 2);
                            comp.add(s2, 2);
                            dp.put(k, false);
                            return false;
                        }
                        comp.add(s2, 2);
                    }
                    comp.add(s1, 2);
                }
                comp.add(bomb, 4);
            }
        }

        if (myTurn && (p.length == 0 || (p.length == 8 && withBomb(p)))) {
            int limit = p.length == 0 ? 3 : p[0] + 1;
            for (int bomb : mine.keys(4, limit)) {
                mine.remove(bomb, 4);
                for (int s1 : mine.keys(2)) {
                    mine.remove(s1, 2);
                    for (int s2 : mine.keys(2)) {
                        mine.remove(s2, 2);
                        if (dfs(false, new int[]{bomb, bomb, bomb, bomb, s1, s1, s2, s2})) {
                            mine.add(bomb, 4);
                            mine.add(s1, 2);
                            mine.add(s2, 2);
                            dp.put(k, true);
                            res = new int[]{bomb, bomb, bomb, bomb, s1, s1, s2, s2};
                            return true;
                        }
                        mine.add(s2, 2);
                    }
                    mine.add(s1, 2);
                }
                mine.add(bomb, 4);
            }
        }

        // Plane
        if (!myTurn && (p.length == 0 || isPlane(p))) {
            int limit = p.length == 0 ? 3 : p[0] + 1;
            for (int start : comp.keys(3, limit)) {
                int sec = start + 1;
                if (comp.cards.getOrDefault(sec, 0) >= 3) {
                    comp.remove(start, 3);
                    comp.remove(sec, 3);
                    if (!dfs(true, new int[]{start, start, start, sec, sec, sec})) {
                        comp.add(start, 3);
                        comp.add(sec, 3);
                        dp.put(k, false);
                        return false;
                    }
                    comp.add(start, 3);
                    comp.add(sec, 3);
                }
            }
        }

        if (myTurn && (p.length == 0 || isPlane(p))) {
            int limit = p.length == 0 ? 3 : p[0] + 1;
            for (int start : mine.keys(3, limit)) {
                int sec = start + 1;
                if (mine.cards.getOrDefault(sec, 0) >= 3) {
                    mine.remove(start, 3);
                    mine.remove(sec, 3);
                    if (dfs(false, new int[]{start, start, start, sec, sec, sec})) {
                        mine.add(start, 3);
                        mine.add(sec, 3);
                        dp.put(k, true);
                        res = new int[]{start, start, start, sec, sec, sec};
                        return true;
                    }
                    mine.add(start, 3);
                    mine.add(sec, 3);
                }
            }
        }

        // Plane with two singles
        if (!myTurn && (p.length == 0 || (p.length == 6 && withPlane(p)))) {
            int limit = p.length == 0 ? 3 : p[0] + 1;
            for (int start : comp.keys(3, limit)) {
                int sec = start + 1;
                if (comp.cards.getOrDefault(sec, 0) >= 3) {
                    comp.remove(start, 3);
                    comp.remove(sec, 3);
                    for (int s1 : comp.keys()) {
                        comp.remove(s1);
                        for (int s2 : comp.keys()) {
                            comp.remove(s2);
                            if (!dfs(true, new int[]{start, start, start, sec, sec, sec, s1, s2})) {
                                comp.add(start, 3);
                                comp.add(sec, 3);
                                comp.add(s1);
                                comp.add(s2);
                                dp.put(k, false);
                                return false;
                            }
                            comp.add(s2);
                        }
                        comp.add(s1);
                    }
                    comp.add(start, 3);
                    comp.add(sec, 3);
                }
            }
        }

        if (myTurn && (p.length == 0 || (p.length == 6 && withPlane(p)))) {
            int limit = p.length == 0 ? 3 : p[0] + 1;
            for (int start : mine.keys(3, limit)) {
                int sec = start + 1;
                if (mine.cards.getOrDefault(sec, 0) >= 3) {
                    mine.remove(start, 3);
                    mine.remove(sec, 3);
                    for (int s1 : mine.keys()) {
                        mine.remove(s1);
                        for (int s2 : mine.keys()) {
                            mine.remove(s2);
                            if (dfs(false, new int[]{start, start, start, sec, sec, sec, s1, s2})) {
                                mine.add(start, 3);
                                mine.add(sec, 3);
                                mine.add(s1);
                                mine.add(s2);
                                dp.put(k, true);
                                res = new int[]{start, start, start, sec, sec, sec, s1, s2};
                                return true;
                            }
                            mine.add(s2);
                        }
                        mine.add(s1);
                    }
                    mine.add(start, 3);
                    mine.add(sec, 3);
                }
            }
        }

        // Plane with two pairs
        if (!myTurn && (p.length == 0 || (p.length == 8 && withPlane(p)))) {
            int limit = p.length == 0 ? 3 : p[0] + 1;
            for (int start : comp.keys(3, limit)) {
                int sec = start + 1;
                if (comp.cards.getOrDefault(sec, 0) >= 3) {
                    comp.remove(start, 3);
                    comp.remove(sec, 3);
                    for (int s1 : comp.keys(2)) {
                        comp.remove(s1, 2);
                        for (int s2 : comp.keys(2)) {
                            comp.remove(s2, 2);
                            if (!dfs(true, new int[]{start, start, start, sec, sec, sec, s1, s1, s2, s2})) {
                                comp.add(start, 3);
                                comp.add(sec, 3);
                                comp.add(s1, 2);
                                comp.add(s2, 2);
                                dp.put(k, false);
                                return false;
                            }
                            comp.add(s2, 2);
                        }
                        comp.add(s1, 2);
                    }
                    comp.add(start, 3);
                    comp.add(sec, 3);
                }
            }
        }

        if (myTurn && (p.length == 0 || (p.length == 8 && withPlane(p)))) {
            int limit = p.length == 0 ? 3 : p[0] + 1;
            for (int start : mine.keys(3, limit)) {
                int sec = start + 1;
                if (mine.cards.getOrDefault(sec, 0) >= 3) {
                    mine.remove(start, 3);
                    mine.remove(sec, 3);
                    for (int s1 : mine.keys(2)) {
                        mine.remove(s1, 2);
                        for (int s2 : mine.keys(2)) {
                            mine.remove(s2, 2);
                            if (dfs(false, new int[]{start, start, start, sec, sec, sec, s1, s1, s2, s2})) {
                                mine.add(start, 3);
                                mine.add(sec, 3);
                                mine.add(s1, 2);
                                mine.add(s2, 2);
                                dp.put(k, true);
                                res = new int[]{start, start, start, sec, sec, sec, s1, s1, s2, s2};
                                return true;
                            }
                            mine.add(s2, 2);
                        }
                        mine.add(s1, 2);
                    }
                    mine.add(start, 3);
                    mine.add(sec, 3);
                }
            }
        }

        // Straight
        if (!myTurn && (p.length == 0 || isStraight(p))) {
            int limit = p.length == 0 ? 3 : p[0] + 1;
            for (int s1 : comp.keys(1, limit)) {
                int s2 = s1 + 1;
                if (comp.cards.getOrDefault(s2, 0) == 0) {
                    continue;
                }
                int s3 = s2 + 1;
                if (comp.cards.getOrDefault(s3, 0) == 0) {
                    continue;
                }
                int s4 = s3 + 1;
                if (comp.cards.getOrDefault(s4, 0) == 0) {
                    continue;
                }
                int s5 = s4 + 1;
                if (comp.cards.getOrDefault(s5, 0) == 0) {
                    continue;
                }
                if (p.length == 0 || p.length == 5) {
                    comp.removeStraight(s1, s5);
                    if (!dfs(true, straight(s1, s5))) {
                        comp.addStraight(s1, s5);
                        dp.put(k, false);
                        return false;
                    }
                    comp.addStraight(s1, s5);
                }
                if (p.length == 5) {
                    continue;
                }
                int s6 = s5 + 1;
                if (comp.cards.getOrDefault(s6, 0) == 0) {
                    continue;
                }
                if (p.length == 0 || p.length == 6) {
                    comp.removeStraight(s1, s6);
                    if (!dfs(true, straight(s1, s6))) {
                        comp.addStraight(s1, s6);
                        dp.put(k, false);
                        return false;
                    }
                    comp.addStraight(s1, s6);
                }
                if (p.length == 6) {
                    continue;
                }
                int s7 = s6 + 1;
                if (comp.cards.getOrDefault(s7, 0) == 0) {
                    continue;
                }
                if (p.length == 0 || p.length == 7) {
                    comp.removeStraight(s1, s7);
                    if (!dfs(true, straight(s1, s7))) {
                        comp.addStraight(s1, s7);
                        dp.put(k, false);
                        return false;
                    }
                    comp.addStraight(s1, s7);
                }
                if (p.length == 7) {
                    continue;
                }
                int s8 = s7 + 1;
                if (comp.cards.getOrDefault(s8, 0) < 1) {
                    continue;
                }
                if (p.length == 0 || p.length == 8) {
                    comp.removeStraight(s1, s8);
                    if (!dfs(true, straight(s1, s8))) {
                        comp.addStraight(s1, s8);
                        dp.put(k, false);
                        return false;
                    }
                    comp.addStraight(s1, s8);
                }
                if (p.length == 8) {
                    continue;
                }
                int s9 = s8 + 1;
                if (comp.cards.getOrDefault(s9, 0) == 0) {
                    continue;
                }
                if (p.length == 0 || p.length == 9) {
                    comp.removeStraight(s1, s9);
                    if (!dfs(true, straight(s1, s9))) {
                        comp.addStraight(s1, s9);
                        dp.put(k, false);
                        return false;
                    }
                    comp.addStraight(s1, s9);
                }
                if (p.length == 9) {
                    continue;
                }
                int s10 = s9 + 1;
                if (comp.cards.getOrDefault(s10, 0) == 0) {
                    continue;
                }
                if (p.length == 0 || p.length == 10) {
                    comp.removeStraight(s1, s10);
                    if (!dfs(true, straight(s1, s10))) {
                        comp.addStraight(s1, s10);
                        dp.put(k, false);
                        return false;
                    }
                    comp.addStraight(s1, s10);
                }
                if (p.length == 10) {
                    continue;
                }
                int s11 = s10 + 1;
                if (comp.cards.getOrDefault(s11, 0) == 0) {
                    continue;
                }
                if (p.length == 0 || p.length == 11) {
                    comp.removeStraight(s1, s11);
                    if (!dfs(true, straight(s1, s11))) {
                        comp.addStraight(s1, s11);
                        dp.put(k, false);
                        return false;
                    }
                    comp.addStraight(s1, s11);
                }
                if (p.length == 11) {
                    continue;
                }
                int s12 = s11 + 1;
                if (comp.cards.getOrDefault(s12, 0) == 0) {
                    continue;
                }
                if (p.length == 0 || p.length == 12) {
                    comp.removeStraight(s1, s12);
                    if (!dfs(true, straight(s1, s12))) {
                        comp.addStraight(s1, s12);
                        dp.put(k, false);
                        return false;
                    }
                    comp.addStraight(s1, s12);
                }
            }
        }

        if (myTurn && (p.length == 0 || isStraight(p))) {
            int limit = p.length == 0 ? 3 : p[0] + 1;
            for (int s1 : mine.keys(1, limit)) {
                int s2 = s1 + 1;
                if (mine.cards.getOrDefault(s2, 0) == 0) {
                    continue;
                }
                int s3 = s2 + 1;
                if (mine.cards.getOrDefault(s3, 0) == 0) {
                    continue;
                }
                int s4 = s3 + 1;
                if (mine.cards.getOrDefault(s4, 0) == 0) {
                    continue;
                }
                int s5 = s4 + 1;
                if (mine.cards.getOrDefault(s5, 0) == 0) {
                    continue;
                }
                if (p.length == 0 || p.length == 5) {
                    mine.removeStraight(s1, s5);
                    if (dfs(false, straight(s1, s5))) {
                        mine.addStraight(s1, s5);
                        dp.put(k, true);
                        res = straight(s1, s5);
                        return true;
                    }
                    mine.addStraight(s1, s5);
                }
                if (p.length == 5) {
                    continue;
                }
                int s6 = s5 + 1;
                if (mine.cards.getOrDefault(s6, 0) == 0) {
                    continue;
                }
                if (p.length == 0 || p.length == 6) {
                    mine.removeStraight(s1, s6);
                    if (dfs(false, straight(s1, s6))) {
                        mine.addStraight(s1, s6);
                        dp.put(k, true);
                        res = straight(s1, s6);
                        return true;
                    }
                    mine.addStraight(s1, s6);
                }
                if (p.length == 6) {
                    continue;
                }
                int s7 = s6 + 1;
                if (mine.cards.getOrDefault(s7, 0) == 0) {
                    continue;
                }
                if (p.length == 0 || p.length == 7) {
                    mine.removeStraight(s1, s7);
                    if (dfs(false, straight(s1, s7))) {
                        mine.addStraight(s1, s7);
                        dp.put(k, true);
                        res = straight(s1, s7);
                        return true;
                    }
                    mine.addStraight(s1, s7);
                }
                if (p.length == 7) {
                    continue;
                }
                int s8 = s7 + 1;
                if (mine.cards.getOrDefault(s8, 0) == 0) {
                    continue;
                }
                if (p.length == 0 || p.length == 8) {
                    mine.removeStraight(s1, s8);
                    if (dfs(false, straight(s1, s8))) {
                        mine.addStraight(s1, s8);
                        dp.put(k, true);
                        res = straight(s1, s8);
                        return true;
                    }
                    mine.addStraight(s1, s8);
                }
                if (p.length == 8) {
                    continue;
                }
                int s9 = s8 + 1;
                if (mine.cards.getOrDefault(s9, 0) == 0) {
                    continue;
                }
                if (p.length == 0 || p.length == 9) {
                    mine.removeStraight(s1, s9);
                    if (dfs(false, straight(s1, s9))) {
                        mine.addStraight(s1, s9);
                        dp.put(k, true);
                        res = straight(s1, s9);
                        return true;
                    }
                    mine.addStraight(s1, s9);
                }
                if (p.length == 9) {
                    continue;
                }
                int s10 = s9 + 1;
                if (mine.cards.getOrDefault(s10, 0) == 0) {
                    continue;
                }
                if (p.length == 0 || p.length == 10) {
                    mine.removeStraight(s1, s10);
                    if (dfs(false, straight(s1, s10))) {
                        mine.addStraight(s1, s10);
                        dp.put(k, true);
                        res = straight(s1, s10);
                        return true;
                    }
                    mine.addStraight(s1, s10);
                }
                if (p.length == 10) {
                    continue;
                }
                int s11 = s10 + 1;
                if (mine.cards.getOrDefault(s11, 0) == 0) {
                    continue;
                }
                if (p.length == 0 || p.length == 11) {
                    mine.removeStraight(s1, s11);
                    if (dfs(false, straight(s1, s11))) {
                        mine.addStraight(s1, s11);
                        dp.put(k, true);
                        res = straight(s1, s11);
                        return true;
                    }
                    mine.addStraight(s1, s11);
                }
                if (p.length == 11) {
                    continue;
                }
                int s12 = s11 + 1;
                if (mine.cards.getOrDefault(s12, 0) == 0) {
                    continue;
                }
                if (p.length == 0 || p.length == 12) {
                    mine.removeStraight(s1, s12);
                    if (dfs(false, straight(s1, s12))) {
                        mine.addStraight(s1, s12);
                        dp.put(k, true);
                        res = straight(s1, s12);
                        return true;
                    }
                    mine.addStraight(s1, s12);
                }
            }
        }

        // Pair seq
        if (!myTurn && (p.length == 0 || isPairSeq(p))) {
            int limit = p.length == 0 ? 3 : p[0] + 1;
            for (int s1 : comp.keys(2, limit)) {
                int s2 = s1 + 1;
                if (comp.cards.getOrDefault(s2, 0) < 2) {
                    continue;
                }
                int s3 = s2 + 1;
                if (comp.cards.getOrDefault(s2, 0) < 2) {
                    continue;
                }
                if (p.length == 0 || p.length == 6) {
                    comp.removeStraight(s1, s3);
                    comp.removeStraight(s1, s3);
                    if (!dfs(true, pairStraight(s1, s3))) {
                        comp.addStraight(s1, s3);
                        comp.addStraight(s1, s3);
                        dp.put(k, false);
                        return false;
                    }
                    comp.addStraight(s1, s3);
                    comp.addStraight(s1, s3);
                }
                if (p.length == 6) {
                    continue;
                }
                int s4 = s3 + 1;
                if (comp.cards.getOrDefault(s4, 0) < 2) {
                    continue;
                }
                if (p.length == 0 || p.length == 8) {
                    comp.removeStraight(s1, s4);
                    comp.removeStraight(s1, s4);
                    if (!dfs(true, pairStraight(s1, s4))) {
                        comp.addStraight(s1, s4);
                        comp.addStraight(s1, s4);
                        dp.put(k, false);
                        return false;
                    }
                    comp.addStraight(s1, s4);
                    comp.addStraight(s1, s4);
                }
                if (p.length == 8) {
                    continue;
                }
                int s5 = s4 + 1;
                if (comp.cards.getOrDefault(s5, 0) < 2) {
                    continue;
                }
                if (p.length == 0 || p.length == 10) {
                    comp.removeStraight(s1, s5);
                    comp.removeStraight(s1, s5);
                    if (!dfs(true, pairStraight(s1, s5))) {
                        comp.addStraight(s1, s5);
                        comp.addStraight(s1, s5);
                        dp.put(k, false);
                        return false;
                    }
                    comp.addStraight(s1, s5);
                    comp.addStraight(s1, s5);
                }
                if (p.length == 10) {
                    continue;
                }
                int s6 = s5 + 1;
                if (comp.cards.getOrDefault(s6, 0) < 2) {
                    continue;
                }
                if (p.length == 0 || p.length == 12) {
                    comp.removeStraight(s1, s6);
                    comp.removeStraight(s1, s6);
                    if (!dfs(true, pairStraight(s1, s6))) {
                        comp.addStraight(s1, s6);
                        comp.addStraight(s1, s6);
                        dp.put(k, false);
                        return false;
                    }
                    comp.addStraight(s1, s6);
                    comp.addStraight(s1, s6);
                }
                if (p.length == 12) {
                    continue;
                }
                int s7 = s6 + 1;
                if (comp.cards.getOrDefault(s7, 0) < 2) {
                    continue;
                }
                if (p.length == 0 || p.length == 14) {
                    comp.removeStraight(s1, s7);
                    comp.removeStraight(s1, s7);
                    if (!dfs(true, pairStraight(s1, s7))) {
                        comp.addStraight(s1, s7);
                        comp.addStraight(s1, s7);
                        dp.put(k, false);
                        return false;
                    }
                    comp.addStraight(s1, s7);
                    comp.addStraight(s1, s7);
                }
                if (p.length == 14) {
                    continue;
                }
                int s8 = s7 + 1;
                if (comp.cards.getOrDefault(s8, 0) < 2) {
                    continue;
                }
                if (p.length == 0 || p.length == 16) {
                    comp.removeStraight(s1, s8);
                    comp.removeStraight(s1, s8);
                    if (!dfs(true, pairStraight(s1, s8))) {
                        comp.addStraight(s1, s8);
                        comp.addStraight(s1, s8);
                        dp.put(k, false);
                        return false;
                    }
                    comp.addStraight(s1, s8);
                    comp.addStraight(s1, s8);
                }
                if (p.length == 16) {
                    continue;
                }
                int s9 = s8 + 1;
                if (comp.cards.getOrDefault(s9, 0) < 2) {
                    continue;
                }
                if (p.length == 0 || p.length == 18) {
                    comp.removeStraight(s1, s9);
                    comp.removeStraight(s1, s9);
                    if (!dfs(true, pairStraight(s1, s9))) {
                        comp.addStraight(s1, s9);
                        comp.addStraight(s1, s9);
                        dp.put(k, false);
                        return false;
                    }
                    comp.addStraight(s1, s9);
                    comp.addStraight(s1, s9);
                }
                if (p.length == 18) {
                    continue;
                }
                int s10 = s9 + 1;
                if (comp.cards.getOrDefault(s10, 0) < 2) {
                    continue;
                }
                if (p.length == 0 || p.length == 20) {
                    comp.removeStraight(s1, s10);
                    comp.removeStraight(s1, s10);
                    if (!dfs(true, pairStraight(s1, s10))) {
                        comp.addStraight(s1, s10);
                        comp.addStraight(s1, s10);
                        dp.put(k, false);
                        return false;
                    }
                    comp.addStraight(s1, s10);
                    comp.addStraight(s1, s10);
                }
            }
        }
        
        if (myTurn && (p.length == 0 || isPairSeq(p))) {
            int limit = p.length == 0 ? 3 : p[0] + 1;
            for (int s1 : mine.keys(2, limit)) {
                int s2 = s1 + 1;
                if (mine.cards.getOrDefault(s2, 0) < 2) {
                    continue;
                }
                int s3 = s2 + 1;
                if (mine.cards.getOrDefault(s2, 0) < 2) {
                    continue;
                }
                if (p.length == 0 || p.length == 6) {
                    mine.removeStraight(s1, s3);
                    mine.removeStraight(s1, s3);
                    if (dfs(false, pairStraight(s1, s3))) {
                        mine.addStraight(s1, s3);
                        mine.addStraight(s1, s3);
                        dp.put(k, true);
                        res = pairStraight(s1, s3);
                        return true;
                    }
                    mine.addStraight(s1, s3);
                    mine.addStraight(s1, s3);
                }
                if (p.length == 6) {
                    continue;
                }
                int s4 = s3 + 1;
                if (mine.cards.getOrDefault(s4, 0) < 2) {
                    continue;
                }
                if (p.length == 0 || p.length == 8) {
                    mine.removeStraight(s1, s4);
                    mine.removeStraight(s1, s4);
                    if (dfs(false, pairStraight(s1, s4))) {
                        mine.addStraight(s1, s4);
                        mine.addStraight(s1, s4);
                        dp.put(k, true);
                        res = pairStraight(s1, s4);
                        return true;
                    }
                    mine.addStraight(s1, s4);
                    mine.addStraight(s1, s4);
                }
                if (p.length == 8) {
                    continue;
                }
                int s5 = s4 + 1;
                if (mine.cards.getOrDefault(s5, 0) < 2) {
                    continue;
                }
                if (p.length == 0 || p.length == 10) {
                    mine.removeStraight(s1, s5);
                    mine.removeStraight(s1, s5);
                    if (dfs(false, pairStraight(s1, s5))) {
                        mine.addStraight(s1, s5);
                        mine.addStraight(s1, s5);
                        dp.put(k, true);
                        res = pairStraight(s1, s5);
                        return true;
                    }
                    mine.addStraight(s1, s5);
                    mine.addStraight(s1, s5);
                }
                if (p.length == 10) {
                    continue;
                }
                int s6 = s5 + 1;
                if (mine.cards.getOrDefault(s6, 0) < 2) {
                    continue;
                }
                if (p.length == 0 || p.length == 12) {
                    mine.removeStraight(s1, s6);
                    mine.removeStraight(s1, s6);
                    if (dfs(false, pairStraight(s1, s6))) {
                        mine.addStraight(s1, s6);
                        mine.addStraight(s1, s6);
                        dp.put(k, true);
                        res = pairStraight(s1, s6);
                        return true;
                    }
                    mine.addStraight(s1, s6);
                    mine.addStraight(s1, s6);
                }
                if (p.length == 12) {
                    continue;
                }
                int s7 = s6 + 1;
                if (mine.cards.getOrDefault(s7, 0) < 2) {
                    continue;
                }
                if (p.length == 0 || p.length == 14) {
                    mine.removeStraight(s1, s7);
                    mine.removeStraight(s1, s7);
                    if (dfs(false, pairStraight(s1, s7))) {
                        mine.addStraight(s1, s7);
                        mine.addStraight(s1, s7);
                        dp.put(k, true);
                        res = pairStraight(s1, s7);
                        return true;
                    }
                    mine.addStraight(s1, s7);
                    mine.addStraight(s1, s7);
                }
                if (p.length == 14) {
                    continue;
                }
                int s8 = s7 + 1;
                if (mine.cards.getOrDefault(s8, 0) < 2) {
                    continue;
                }
                if (p.length == 0 || p.length == 16) {
                    mine.removeStraight(s1, s8);
                    mine.removeStraight(s1, s8);
                    if (dfs(false, pairStraight(s1, s8))) {
                        dp.put(k, true);
                        mine.addStraight(s1, s8);
                        mine.addStraight(s1, s8);
                        res = pairStraight(s1, s8);
                        return true;
                    }
                    mine.addStraight(s1, s8);
                    mine.addStraight(s1, s8);
                }
                if (p.length == 16) {
                    continue;
                }
                int s9 = s8 + 1;
                if (mine.cards.getOrDefault(s9, 0) < 2) {
                    continue;
                }
                if (p.length == 0 || p.length == 18) {
                    mine.removeStraight(s1, s9);
                    mine.removeStraight(s1, s9);
                    if (dfs(false, pairStraight(s1, s9))) {
                        mine.addStraight(s1, s9);
                        mine.addStraight(s1, s9);
                        dp.put(k, true);
                        res = pairStraight(s1, s9);
                        return true;
                    }
                    mine.addStraight(s1, s9);
                    mine.addStraight(s1, s9);
                }
                if (p.length == 18) {
                    continue;
                }
                int s10 = s9 + 1;
                if (mine.cards.getOrDefault(s10, 0) < 2) {
                    continue;
                }
                if (p.length == 0 || p.length == 20) {
                    mine.removeStraight(s1, s10);
                    mine.removeStraight(s1, s10);
                    if (dfs(false, pairStraight(s1, s10))) {
                        mine.addStraight(s1, s10);
                        mine.addStraight(s1, s10);
                        dp.put(k, true);
                        res = pairStraight(s1, s10);
                        return true;
                    }
                    mine.addStraight(s1, s10);
                    mine.addStraight(s1, s10);
                }
            }
        }


        if (myTurn) {
            dp.put(k, false);
            return false;
        }
        dp.put(k, true);
        return true;
    }

    public boolean isBomb(int[] s) {
        if (s.length != 4) {
            return false;
        }
        return s[0] == s[1] && s[1] == s[2] && s[2] == s[3];
    }

    public boolean withBomb(int[] s) {
        if (s.length < 6) {
            return false;
        }
        return s[0] == s[1] && s[1] == s[2] && s[2] == s[3];
    }

    public boolean isFull(int[] s) {
        if (s.length != 5) {
            return false;
        }
        return s[0] == s[1] && s[1] == s[2] && s[3] == s[4];
    }

    public boolean isStraight(int[] s) {
        if (s.length < 5) {
            return false;
        }
        return s[0] + 1 == s[1] && s[1] + 1 == s[2];
    }

    public boolean isPlane(int[] s) {
        if (s.length != 6) {
            return false;
        }
        return s[0] == s[1] && s[1] == s[2] && s[3] == s[4] && s[4] == s[5];
    }

    public boolean withPlane(int[] s) {
        if (s.length <= 6) {
            return false;
        }
        return s[0] == s[1] && s[1] == s[2] && s[3] == s[4] && s[4] == s[5];
    }

    public boolean isPairSeq(int[] s) {
        if (s.length < 6 || s.length % 2 != 0) {
            return false;
        }
        return s[0] + 1 == s[2] && s[2] + 1 == s[4];
    }

    public int[] straight(int start, int end) {
        int length = end - start + 1;
        int[] res = new int[length];
        int cur = start;
        for (int i = 0; i < length; i++) {
            res[i] = cur;
            cur += 1;
        }
        return res;
    }

    public int[] pairStraight(int start, int end) {
        int length = (end - start + 1) * 2;
        int[] res = new int[length];
        int cur = start;
        for (int i = 0; i < length; i += 2) {
            res[i] = cur;
            res[i + 1] = cur;
            cur += 1;
        }
        return res;
    }

    public static void main(String[] args) {
        /*
        comp = [JOKER2,2,A,K,J,J,10,10,9,8,7,7,5]
            mine = [2,A,K,Q,Q,J,J,10,10,9,8,5,3]
         */
        int A = 14;
        int K = 13;
        int Q = 12;
        int J = 11;
        int JOKER2 = 17;
        int JOKER = 18;
        Instant start = Instant.now();
        HappyPoker happyPoker = new HappyPoker();
        happyPoker.comp = new Pokers(new int[]{K,K,Q,9,6,6,4,4});
        happyPoker.mine = new Pokers(new int[]{JOKER,7,7,5,5,3});
        System.out.println(happyPoker.dfs(true, new int[]{}));
        System.out.println(Arrays.toString(happyPoker.res));
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        System.out.println(timeElapsed);
    }

}
