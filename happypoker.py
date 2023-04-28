class Pokers:
    def __init__(self, cards):
        modify(cards)
        self.cards = Counter(cards)
        self.state = 0
        for card in cards:
            self.addstate(card)

    def addstate(self, card):
        if card == 18:
            self.state += 5 ** 15
        elif card == 17:
            self.state += 5 ** 14
        elif card == 16:
            self.state += 5
        elif card == 14:
            self.state += 1
        else:
            self.state += 5 ** (card - 1)

    def removestate(self, card):
        if card == 18:
            self.state -= 5 ** 15
        elif card == 17:
            self.state -= 5 ** 14
        elif card == 16:
            self.state -= 5
        elif card == 14:
            self.state -= 1
        else:
            self.state -= 5 ** (card - 1)

    def add(self, card, cnt = 1):
        for _ in range(cnt):
            self.cards[card] += 1
            self.addstate(card)

    def remove(self, card, cnt = 1):
        for _ in range(cnt):
            self.cards[card] -= 1
            self.removestate(card)

    def keys(self, mincnt = 1, limit = 3):
        res = []
        for key, value in self.cards.items():
            if value >= mincnt and key >= limit:
                res.append(key)
        return res

    def addstraight(self, low, high):
        for card in range(low, high + 1):
            self.add(card)

    def removestraight(self, low, high):
        for card in range(low, high + 1):
            self.remove(card)

from typing import Counter
from typing import defaultdict

def issingle(s):
    return isinstance(s, int)

def isbomb(s):
    if issingle(s):
        return False
    if len(s) != 4:
        return False
    return s[0] == s[1] == s[2] == s[3]

def withbomb(s):
    if issingle(s):
        return False
    return s[0] == s[1] == s[2] == s[3]

def isfull(s):
    if issingle(s):
        return False
    if len(s) != 5:
        return False
    return s[0] == s[1] == s[2] and s[3] == s[4]

def isstraight(s):
    if issingle(s) or len(s) < 5:
        return False
    return s[0] + 2 == s[1] + 1 == s[2]

def isplane(s):
    if issingle(s):
        return False
    if len(s) != 6:
        return False
    return s[0] == s[1] == s[2] and s[3] == s[4] == s[5]

def withplane(s):
    if issingle(s):
        return False
    if len(s) < 8:
        return False
    return s[0] == s[1] == s[2] and s[3] == s[4] == s[5]

def ispairseq(s):
    if issingle(s):
        return False
    if len(s) < 6:
        return False
    return s[0] + 2 == s[2] + 1 == s[4]

def tokey(m, c, p, myturn):
    if not p or issingle(p):
        key = (m.state, c.state, p, myturn)
    else:
        key = (m.state, c.state, tuple(p), myturn)
    return key

res = [""]
dp = {}
mp = [True]

def dfs(m, c, myturn, p = None):
    if p == 2:
        p = 16
    if p == 1:
        p = 14
    if mp[0] and p and not issingle(p):
        modify(p)
    mp[0] = False
    if not c.state:
        return False
    if not m.state:
        return True
    key = tokey(m, c, p, myturn)
    if key in dp:
        return dp[key]

    # pass
    if not myturn and p and not dfs(m, c, not myturn, None):
        dp[key] = False
        return False
    # pass
    if myturn and p and dfs(m, c, not myturn):
        res[0] = ["PASS"]
        dp[key] = True
        return True

    # single
    if not p or issingle(p):
        if not myturn:
            limit = 3 if not p else p + 1
            for single in c.keys(1, limit):
                c.remove(single)
                if not dfs(m, c, not myturn, single):
                    c.add(single)
                    dp[key] = False
                    return False
                c.add(single)
        else:
            limit = 3 if not p else p + 1
            for single in m.keys(1, limit):
                m.remove(single)
                if dfs(m, c, not myturn, single):
                    m.add(single)
                    res[0] = [single]
                    dp[key] = True
                    return True
                m.add(single)
    # pair
    if not myturn and (not p or (not issingle(p) and len(p) == 2)):
        limit = 3 if not p else p[0] + 1
        for pair in c.keys(2, limit):
            c.remove(pair, 2)
            if not dfs(m, c, not myturn, [pair, pair]):
                c.add(pair, 2)
                dp[key] = False
                return False
            c.add(pair, 2)

    if myturn and (not p or (not issingle(p) and len(p) == 2)):
        limit = 3 if not p else p[0] + 1
        for pair in m.keys(2, limit):
            m.remove(pair, 2)
            if dfs(m, c, not myturn, [pair, pair]):
                m.add(pair, 2)
                dp[key] = True
                res[0] = [pair, pair]
                return True
            m.add(pair, 2)

    # triple
    if not myturn and (not p or (not issingle(p) and len(p) == 3)):
        limit = 3 if not p else p[0] + 1
        for triple in c.keys(3, limit):
            c.remove(triple, 3)
            if not dfs(m, c, not myturn, [triple, triple, triple]):
                c.add(triple, 3)
                dp[key] = False
                return False
            c.add(triple, 3)

    if myturn and (not p or (not issingle(p) and len(p) == 3)):
        limit = 3 if not p else p[0] + 1
        for triple in m.keys(3, limit):
            m.remove(triple, 3)
            if dfs(m, c, not myturn, [triple, triple, triple]):
                m.add(triple, 3)
                dp[key] = True
                res[0] = [triple, triple, triple]
                return True
            m.add(triple, 3)

    # triple single
    if not myturn and (not p or (not issingle(p) and len(p) == 4 and not isbomb(p))):
        limit = 3 if not p else p[0] + 1
        for triple in c.keys(3, limit):
            c.remove(triple, 3)
            for single in c.keys():
                c.remove(single)
                if not dfs(m, c, not myturn, [triple, triple, triple, single]):
                    c.add(triple, 3)
                    c.add(single)
                    dp[key] = False
                    return False
                c.add(single)
            c.add(triple, 3)

    if myturn and (not p or (not issingle(p) and len(p) == 4 and not isbomb(p))):
        limit = 3 if not p else p[0] + 1
        for triple in m.keys(3, limit):
            m.remove(triple, 3)
            for single in m.keys():
                m.remove(single)
                if dfs(m, c, not myturn, [triple, triple, triple, single]):
                    m.add(triple, 3)
                    m.add(single)
                    dp[key] = True
                    res[0] = [triple, triple, triple, single]
                    return True
                m.add(single)
            m.add(triple, 3)

    # full
    if not myturn and (not p or (not issingle(p) and isfull(p))):
        limit = 3 if not p else p[0] + 1
        for triple in c.keys(3, limit):
            c.remove(triple, 3)
            for pair in c.keys(2):
                c.remove(pair, 2)
                if not dfs(m, c, not myturn, [triple, triple, triple, pair, pair]):
                    c.add(triple, 3)
                    c.add(pair, 2)
                    dp[key] = False
                    return False
                c.add(pair, 2)
            c.add(triple, 3)

    if myturn and (not p or (not issingle(p) and isfull(p))):
        limit = 3 if not p else p[0] + 1
        for triple in m.keys(3, limit):
            m.remove(triple, 3)
            for pair in m.keys(2):
                m.remove(pair, 2)
                if dfs(m, c, not myturn, [triple, triple, triple, pair, pair]):
                    m.add(triple, 3)
                    m.add(pair, 2)
                    dp[key] = True
                    res[0] = [triple, triple, triple, pair, pair]
                    return True
                m.add(pair, 2)
            m.add(triple, 3)

    # bomb
    if not myturn:
        if c.cards[18] and c.cards[17]:
            c.remove(18)
            c.remove(17)
            if not dfs(m, c, not myturn, [18, 17]):
                c.add(18)
                c.add(17)
                dp[key] = False
                return False
            c.add(18)
            c.add(17)

    if myturn:
        if m.cards[18] and m.cards[17]:
            m.remove(18)
            m.remove(17)
            if dfs(m, c, not myturn, [18, 17]):
                m.add(18)
                m.add(17)
                dp[key] = True
                res[0] = ["王炸"]
                return True
            m.add(18)
            m.add(17)

    if not myturn:
        limit = 3 if not p or not isbomb(p) else p[0] + 1
        for bomb in c.keys(4, limit):
            c.remove(bomb, 4)
            if not dfs(m, c, not myturn, [bomb, bomb, bomb, bomb]):
                c.add(bomb, 4)
                dp[key] = False
                return False
            c.add(bomb, 4)

    if myturn:
        limit = 3 if not p or not isbomb(p) else p[0] + 1
        for bomb in m.keys(4, limit):
            m.remove(bomb, 4)
            if dfs(m, c, not myturn, [bomb, bomb, bomb, bomb]):
                m.add(bomb, 4)
                res[0] = [bomb, bomb, bomb, bomb]
                dp[key] = True
                return True
            m.add(bomb, 4)

    # bomb two single
    if not myturn and (not p or (not issingle(p) and len(p) == 6 and withbomb(p))):
        limit = 3 if not p else p[0] + 1
        for bomb in c.keys(4, limit):
            c.remove(bomb, 4)
            for s1 in c.keys():
                c.remove(s1)
                for s2 in c.keys():
                    c.remove(s2)
                    if not dfs(m, c, not myturn, [bomb, bomb, bomb, bomb, s1, s2]):
                        c.add(bomb, 4)
                        c.add(s1)
                        c.add(s2)
                        dp[key] = False
                        return False
                    c.add(s2)
                c.add(s1)
            c.add(bomb, 4)

    if myturn and (not p or (not issingle(p) and len(p) == 6 and withbomb(p))):
        limit = 3 if not p else p[0] + 1
        for bomb in m.keys(4, limit):
            m.remove(bomb, 4)
            for s1 in m.keys():
                m.remove(s1)
                for s2 in m.keys():
                    m.remove(s2)
                    if dfs(m, c, not myturn, [bomb, bomb, bomb, bomb, s1, s2]):
                        m.add(bomb, 4)
                        m.add(s1)
                        m.add(s2)
                        dp[key] = True
                        res[0] = [bomb, bomb, bomb, bomb, s1, s2]
                        return True
                    m.add(s2)
                m.add(s1)
            m.add(bomb, 4)

    # bomb two pair
    if not myturn and (not p or (not issingle(p) and len(p) == 8 and withbomb(p))):
        limit = 3 if not p else p[0] + 1
        for bomb in c.keys(4, limit):
            c.remove(bomb, 4)
            for p1 in c.keys(2):
                c.remove(p1, 2)
                for p2 in c.keys(2):
                    c.remove(p2, 2)
                    if not dfs(m, c, not myturn, [bomb, bomb, bomb, bomb, p1, p1, p2, p2]):
                        c.add(bomb, 4)
                        c.add(p1, 2)
                        c.add(p2, 2)
                        dp[key] = False
                        return False
                    c.add(p2, 2)
                c.add(p1, 2)
            c.add(bomb, 4)

    if myturn and (not p or (not issingle(p) and len(p) == 8 and withbomb(p))):
        limit = 3 if not p else p[0] + 1
        for bomb in m.keys(4, limit):
            m.remove(bomb, 4)
            for p1 in m.keys(2):
                m.remove(p1, 2)
                for p2 in m.keys(2):
                    m.remove(p2, 2)
                    if dfs(m, c, not myturn, [bomb, bomb, bomb, bomb, p1, p1, p2, p2]):
                        m.add(bomb, 4)
                        m.add(p1, 2)
                        m.add(p2, 2)
                        dp[key] = True
                        res[0] = [bomb, bomb, bomb, bomb, p1, p1, p2, p2]
                        return True
                    m.add(p2, 2)
                m.add(p1, 2)
            m.add(bomb, 4)

    # 12345
    if not myturn and (not p or isstraight(p)):
        limit = 3 if not p else p[0] + 1
        for s1 in c.keys(1, limit):
            if not c.cards[s1]:
                continue
            s2 = s1 + 1
            if not c.cards[s2]:
                continue
            s3 = s2 + 1
            if not c.cards[s3]:
                continue
            s4 = s3 + 1
            if not c.cards[s4]:
                continue
            s5 = s4 + 1
            if not c.cards[s5]:
                continue
            if p and (len(p) == 5 and s5 <= p[-1]):
                continue
            if not p or len(p) == 5:
                c.removestraight(s1, s5)
                if not dfs(m, c, not myturn, [s1,s2,s3,s4,s5]):
                    c.addstraight(s1, s5)
                    dp[key] = False
                    return False
                c.addstraight(s1, s5)
            s6 = s5 + 1
            if not c.cards[s6]:
                continue
            if p and (len(p) == 6 and s6 <= p[-1]):
                continue
            if not p or len(p) == 6:
                c.removestraight(s1, s6)
                if not dfs(m, c, not myturn, [s1,s2,s3,s4,s5,s6]):
                    c.addstraight(s1, s6)
                    dp[key] = False
                    return False
                c.addstraight(s1, s6)
            s7 = s6 + 1
            if not c.cards[s7]:
                continue
            if p and (len(p) == 7 and s7 <= p[-1]):
                continue
            if not p or len(p) == 7:
                c.removestraight(s1, s7)
                if not dfs(m, c, not myturn, [s1,s2,s3,s4,s5,s6,s7]):
                    c.addstraight(s1, s7)
                    dp[key] = False
                    return False
                c.addstraight(s1, s7)
            s8 = s7 + 1
            if not c.cards[s8]:
                continue
            if p and (len(p) == 8 and s8 <= p[-1]):
                continue
            if not p or len(p) == 8:
                c.removestraight(s1, s8)
                if not dfs(m, c, not myturn, [s1,s2,s3,s4,s5,s6,s7,s8]):
                    c.addstraight(s1, s8)
                    dp[key] = False
                    return False
                c.addstraight(s1, s8)
            s9 = s8 + 1
            if not c.cards[s9]:
                continue
            if p and (len(p) == 9 and s9 <= p[-1]):
                continue
            if not p or len(p) == 9:
                c.removestraight(s1, s9)
                if not dfs(m, c, not myturn, [s1,s2,s3,s4,s5,s6,s7,s8,s9]):
                    c.addstraight(s1, s9)
                    dp[key] = False
                    return False
                c.addstraight(s1, s9)
            s10 = s9 + 1
            if not c.cards[s10]:
                continue
            if p and (len(p) == 10 and s10 <= p[-1]):
                continue
            if not p or len(p) == 10:
                c.removestraight(s1, s10)
                if not dfs(m, c, not myturn, [s1,s2,s3,s4,s5,s6,s7,s8,s9,s10]):
                    c.addstraight(s1, s10)
                    dp[key] = False
                    return False
                c.addstraight(s1, s10)
            s11 = s10 + 1
            if not c.cards[s11]:
                continue
            if p and (len(p) == 11 and s11 <= p[-1]):
                continue
            if not p or len(p) == 11:
                c.removestraight(s1, s11)
                if not dfs(m, c, not myturn, [s1,s2,s3,s4,s5,s6,s7,s8,s9,s10,s11]):
                    c.addstraight(s1, s11)
                    dp[key] = False
                    return False
                c.addstraight(s1, s11)
            s12 = s11 + 1
            if not c.cards[s12]:
                continue
            if p and (len(p) == 12 and s12 <= p[-1]):
                continue
            if not p or len(p) == 12:
                c.removestraight(s1, s12)
                if not dfs(m, c, not myturn, [s1,s2,s3,s4,s5,s6,s7,s8,s9,s10,s11,s12]):
                    c.addstraight(s1, s12)
                    dp[key] = False
                    return False
                c.addstraight(s1, s12)

    if myturn and (not p or isstraight(p)):
        limit = 3 if not p else p[0] + 1
        for s1 in m.keys(1, limit):
            if not m.cards[s1]:
                continue
            s2 = s1 + 1
            if not m.cards[s2]:
                continue
            s3 = s2 + 1
            if not m.cards[s3]:
                continue
            s4 = s3 + 1
            if not m.cards[s4]:
                continue
            s5 = s4 + 1
            if not m.cards[s5]:
                continue
            if p and (len(p) == 5 and s5 <= p[-1]):
                continue
            if not p or len(p) == 5:
                m.removestraight(s1, s5)
                if dfs(m, c, not myturn, [s1,s2,s3,s4,s5]):
                    m.addstraight(s1, s5)
                    dp[key] = True
                    res[0] = [s1,s2,s3,s4,s5]
                    return True
                m.addstraight(s1, s5)
            s6 = s5 + 1
            if not m.cards[s6]:
                continue
            if p and (len(p) == 6 and s6 <= p[-1]):
                continue
            if not p or len(p) == 6:
                m.removestraight(s1, s6)
                if dfs(m, c, not myturn, [s1,s2,s3,s4,s5,s6]):
                    m.addstraight(s1, s6)
                    dp[key] = True
                    res[0] = [s1,s2,s3,s4,s5,s6]
                    return True
                m.addstraight(s1, s6)
            s7 = s6 + 1
            if not m.cards[s7]:
                continue
            if p and (len(p) == 7 and s7 <= p[-1]):
                continue
            if not p or len(p) == 7:
                m.removestraight(s1, s7)
                if dfs(m, c, not myturn, [s1,s2,s3,s4,s5,s6,s7]):
                    m.addstraight(s1, s7)
                    dp[key] = True
                    res[0] = [s1,s2,s3,s4,s5,s6,s7]
                    return True
                m.addstraight(s1, s7)
            s8 = s7 + 1
            if not m.cards[s8]:
                continue
            if p and (len(p) == 8 and s8 <= p[-1]):
                continue
            if not p or len(p) == 8:
                m.removestraight(s1, s8)
                if dfs(m, c, not myturn, [s1,s2,s3,s4,s5,s6,s7,s8]):
                    m.addstraight(s1, s8)
                    dp[key] = True
                    res[0] = [s1,s2,s3,s4,s5,s6,s7,s8]
                    return True
                m.addstraight(s1, s8)
            s9 = s8 + 1
            if not m.cards[s9]:
                continue
            if p and (len(p) == 9 and s9 <= p[-1]):
                continue
            if not p or len(p) == 9:
                m.removestraight(s1, s9)
                if dfs(m, c, not myturn, [s1,s2,s3,s4,s5,s6,s7,s8,s9]):
                    m.addstraight(s1, s9)
                    dp[key] = True
                    res[0] = [s1,s2,s3,s4,s5,s6,s7,s8,s9]
                    return True
                m.addstraight(s1, s9)
            s10 = s9 + 1
            if not m.cards[s10]:
                continue
            if p and (len(p) == 10 and s10 <= p[-1]):
                continue
            if not p or len(p) == 10:
                m.removestraight(s1, s10)
                if dfs(m, c, not myturn, [s1,s2,s3,s4,s5,s6,s7,s8,s9,s10]):
                    m.addstraight(s1, s10)
                    dp[key] = True
                    res[0] = [s1,s2,s3,s4,s5,s6,s7,s8,s10]
                    return True
                m.addstraight(s1, s10)
            s11 = s10 + 1
            if not m.cards[s11]:
                continue
            if p and (len(p) == 11 and s11 <= p[-1]):
                continue
            if not p or len(p) == 11:
                m.removestraight(s1, s11)
                if dfs(m, c, not myturn, [s1,s2,s3,s4,s5,s6,s7,s8,s9,s10,s11]):
                    m.addstraight(s1, s11)
                    dp[key] = True
                    res[0] = [s1,s2,s3,s4,s5,s6,s7,s8,s10,s11]
                    return True
                m.addstraight(s1, s11)
            s12 = s11 + 1
            if not m.cards[s12]:
                continue
            if p and (len(p) == 12 and s12 <= p[-1]):
                continue
            if not p or len(p) == 12:
                m.removestraight(s1, s12)
                if dfs(m, c, not myturn, [s1,s2,s3,s4,s5,s6,s7,s8,s9,s10,s11,s12]):
                    m.removestraight(s1, s12)
                    dp[key] = True
                    res[0] = [s1,s2,s3,s4,s5,s6,s7,s8,s10,s11,s12]
                    return True
                m.addstraight(s1, s12)

    # plane
    if not myturn and (not p or isplane(p)):
        limit = 3 if not p else p[0] + 1
        for start in c.keys(3, limit):
            sec = start + 1
            if c.cards[sec] >= 3:
                c.remove(start, 3)
                c.remove(sec, 3)
                if not dfs(m, c, not myturn, [start, start, start, sec, sec, sec]):
                    c.add(start, 3)
                    c.add(sec, 3)
                    dp[key] = False
                    return False
                c.add(start, 3)
                c.add(sec, 3)

    if myturn and (not p or isplane(p)):
        limit = 3 if not p else p[0] + 1
        for start in m.keys(3, limit):
            sec = start + 1
            if m.cards[sec] >= 3:
                m.remove(start, 3)
                m.remove(sec, 3)
                if dfs(m, c, not myturn, [start, start, start, sec, sec, sec]):
                    m.add(start, 3)
                    m.add(sec, 3)
                    dp[key] = True
                    res[0] = [start, start, start, sec, sec, sec]
                    return True
                m.add(start, 3)
                m.add(sec, 3)

    # plane with two single
    if not myturn and (not p or (not issingle(p) and len(p) == 8 and withplane(p))):
        limit = 3 if not p else p[0] + 1
        for start in c.keys(3, limit):
            sec = start + 1
            if c.cards[sec] >= 3:
                c.cards[start] -= 3
                c.cards[sec] -= 3
                for s1 in c.keys():
                    c.remove(s1)
                    for s2 in c.keys():
                        c.remove(s2)
                        if not dfs(m, c, not myturn, [start, start, start, sec, sec, sec, s1, s2]):
                            c.add(start, 3)
                            c.add(sec, 3)
                            c.add(s1)
                            c.add(s2)
                            dp[key] = False
                            return False
                        c.add(s2)
                    c.add(s1)
                c.add(start, 3)
                c.add(sec, 3)

    if myturn and (not p or (not issingle(p) and len(p) == 8 and withplane(p))):
        limit = 3 if not p else p[0] + 1
        for start in m.keys(3, limit):
            sec = start + 1
            if m.cards[start] >= 3 and m.cards[sec] >= 3:
                m.remove(start, 3)
                m.remove(sec, 3)
                for s1 in m.keys():
                    m.remove(s1)
                    for s2 in m.keys():
                        m.remove(s2)
                        if dfs(m, c, not myturn, [start, start, start, sec, sec, sec, s1, s2]):
                            m.add(start, 3)
                            m.add(sec, 3)
                            m.add(s1)
                            m.add(s2)
                            dp[key] = True
                            res[0] = [start, start, start, sec, sec, sec, s1, s2]
                            return True
                        m.add(s2)
                    m.add(s1)
                m.add(start, 3)
                m.add(sec, 3)

    # plane with two pair
    if not myturn and (not p or (not issingle(p) and len(p) == 10 and withplane(p))):
        limit = 3 if not p else p[0] + 1
        for start in c.keys(3, limit):
            sec = start + 1
            if c.cards[sec] >= 3:
                c.remove(start, 3)
                c.remove(sec, 3)
                for p1 in c.keys(2):
                    c.remove(p1, 2)
                    for p2 in c.keys(2):
                        c.remove(p2, 2)
                        if not dfs(m, c, not myturn, [start, start, start, sec, sec, sec, p1, p1, p2, p2]):
                            c.add(start, 3)
                            c.add(sec, 3)
                            c.add(p1, 2)
                            c.add(p2, 2)
                            dp[key] = False
                            return False
                        c.add(p2, 2)
                    c.add(p1, 2)
                c.add(start, 3)
                c.add(sec, 3)

    if myturn and (not p or (not issingle(p) and len(p) == 10 and withplane(p))):
        limit = 3 if not p else p[0] + 1
        for start in m.keys(3, limit):
            sec = start + 1
            if m.cards[sec] >= 3:
                m.remove(start, 3)
                m.remove(sec, 3)
                for p1 in m.keys(2):
                    m.remove(p1, 2)
                    for p2 in m.keys(2):
                        m.remove(p2, 2)
                        if dfs(m, c, not myturn, [start, start, start, sec, sec, sec, p1, p1, p2, p2]):
                            m.add(start, 3)
                            m.add(sec, 3)
                            m.add(p1, 2)
                            m.add(p2, 2)
                            dp[key] = True
                            res[0] = [start, start, start, sec, sec, sec, p1, p1, p2, p2]
                            return True
                        m.add(p2, 2)
                    m.add(p1, 2)
                m.add(start, 3)
                m.add(sec, 3)

    # 112233
    if not myturn and (not p or ispairseq(p)):
        limit = 3 if not p else p[0] + 1
        for s1 in mine.keys(2, limit):
            if c.cards[s1] < 2:
                continue
            s2 = s1 + 1
            if c.cards[s2] < 2:
                continue
            s3 = s2 + 1
            if c.cards[s3] < 2:
                continue
            if p and s3 <= max(p) and len(p) == 6:
                continue
            if not p or len(p) == 6:
                c.removestraight(s1, s3)
                c.removestraight(s1, s3)
                if not dfs(m, c, not myturn, [s1,s1,s2,s2,s3,s3]):
                    c.addstraight(s1, s3)
                    c.addstraight(s1, s3)
                    dp[key] = False
                    return False
                c.addstraight(s1, s3)
                c.addstraight(s1, s3)
            s4 = s3 + 1
            if c.cards[s4] < 2:
                continue
            if p and s4 <= max(p) and len(p) == 8:
                continue
            if not p or len(p) == 8:
                c.removestraight(s1, s4)
                c.removestraight(s1, s4)
                if not dfs(m, c, not myturn, [s1,s1,s2,s2,s3,s3,s4,s4]):
                    c.addstraight(s1, s4)
                    c.addstraight(s1, s4)
                    dp[key] = False
                    return False
                c.addstraight(s1, s4)
                c.addstraight(s1, s4)
            s5 = s4 + 1
            if c.cards[s5] < 2:
                continue
            if p and s5 <= max(p) and len(p) == 10:
                continue
            if not p or len(p) == 10:
                c.removestraight(s1, s5)
                c.removestraight(s1, s5)
                if not dfs(m, c, not myturn, [s1,s1,s2,s2,s3,s3,s4,s4,s5,s5]):
                    c.addstraight(s1, s5)
                    c.addstraight(s1, s5)
                    dp[key] = False
                    return False
                c.addstraight(s1, s5)
                c.addstraight(s1, s5)
            s6 = s5 + 1
            if c.cards[s6] < 2:
                continue
            if p and s6 <= max(p) and len(p) == 12:
                continue
            if not p or len(p) == 12:
                c.removestraight(s1, s6)
                c.removestraight(s1, s6)
                if not dfs(m, c, not myturn, [s1,s1,s2,s2,s3,s3,s4,s4,s5,s5,s6,s6]):
                    c.addstraight(s1, s6)
                    c.addstraight(s1, s6)
                    dp[key] = False
                    return False
                c.addstraight(s1, s6)
                c.addstraight(s1, s6)
            s7 = s6 + 1
            if c.cards[s7] < 2:
                continue
            if p and s7 <= max(p) and len(p) == 14:
                continue
            if not p or len(p) == 14:
                c.removestraight(s1, s7)
                c.removestraight(s1, s7)
                if not dfs(m, c, not myturn, [s1,s1,s2,s2,s3,s3,s4,s4,s5,s5,s6,s6,s7,s7]):
                    c.addstraight(s1, s7)
                    c.addstraight(s1, s7)
                    dp[key] = False
                    return False
                c.addstraight(s1, s7)
                c.addstraight(s1, s7)
            s8 = s7 + 1
            if c.cards[s8] < 2:
                continue
            if p and s8 <= max(p) and len(p) == 16:
                continue
            if not p or len(p) == 16:
                c.removestraight(s1, s8)
                c.removestraight(s1, s8)
                if not dfs(m, c, not myturn, [s1,s1,s2,s2,s3,s3,s4,s4,s5,s5,s6,s6,s7,s7,s8,s8]):
                    c.addstraight(s1, s8)
                    c.addstraight(s1, s8)
                    dp[key] = False
                    return False
                c.addstraight(s1, s8)
                c.addstraight(s1, s8)
            s9 = s8 + 1
            if c.cards[s9] < 2:
                continue
            if p and s9 <= max(p) and len(p) == 18:
                continue
            if not p or len(p) == 18:
                c.removestraight(s1, s9)
                c.removestraight(s1, s9)
                if not dfs(m, c, not myturn, [s1,s1,s2,s2,s3,s3,s4,s4,s5,s5,s6,s6,s7,s7,s8,s8,s9,s9]):
                    c.addstraight(s1, s9)
                    c.addstraight(s1, s9)
                    dp[key] = False
                    return False
                c.addstraight(s1, s9)
                c.addstraight(s1, s9)
            s10 = s9 + 1
            if c.cards[s10] < 2:
                continue
            if p and s10 <= max(p) and len(p) == 20:
                continue
            if not p or len(p) == 20:
                c.removestraight(s1, s10)
                c.removestraight(s1, s10)
                if not dfs(m, c, not myturn, [s1,s1,s2,s2,s3,s3,s4,s4,s5,s5,s6,s6,s7,s7,s8,s8,s9,s9,s10,s10]):
                    c.addstraight(s1, s10)
                    c.addstraight(s1, s10)
                    dp[key] = False
                    return False
                c.addstraight(s1, s10)
                c.addstraight(s1, s10)

    if myturn and (not p or ispairseq(p)):
        limit = 3 if not p else p[0] + 1
        for s1 in m.keys(1, limit):
            if m.cards[s1] < 2:
                continue
            s2 = s1 + 1
            if m.cards[s2] < 2:
                continue
            s3 = s2 + 1
            if m.cards[s3] < 2:
                continue
            if p and s3 <= max(p) and len(p) == 6:
                continue
            if not p or len(p) == 6:
                m.removestraight(s1, s3)
                m.removestraight(s1, s3)
                if dfs(m, c, not myturn, [s1,s1,s2,s2,s3,s3]):
                    m.addstraight(s1, s3)
                    m.addstraight(s1, s3)
                    dp[key] = True
                    res[0] = [s1,s1,s2,s2,s3,s3]
                    return True
                m.addstraight(s1, s3)
                m.addstraight(s1, s3)
            s4 = s3 + 1
            if m.cards[s4] < 2:
                continue
            if p and s4 <= max(p) and len(p) == 8:
                continue
            if not p or len(p) == 8:
                m.removestraight(s1, s4)
                m.removestraight(s1, s4)
                if dfs(m, c, not myturn, [s1,s1,s2,s2,s3,s3,s4,s4]):
                    m.addstraight(s1, s4)
                    m.addstraight(s1, s4)
                    dp[key] = True
                    res[0] = [s1,s1,s2,s2,s3,s3,s4,s4]
                    return True
                m.addstraight(s1, s4)
                m.addstraight(s1, s4)
            s5 = s4 + 1
            if m.cards[s5] < 2:
                continue
            if p and s5 <= max(p) and len(p) == 10:
                continue
            if not p or len(p) == 10:
                m.removestraight(s1, s5)
                m.removestraight(s1, s5)
                if dfs(m, c, not myturn, [s1,s1,s2,s2,s3,s3,s4,s4,s5,s5]):
                    m.addstraight(s1, s5)
                    m.addstraight(s1, s5)
                    dp[key] = True
                    res[0] = [s1,s1,s2,s2,s3,s3,s4,s4,s5,s5]
                    return True
                m.addstraight(s1, s5)
                m.addstraight(s1, s5)
            s6 = s5 + 1
            if m.cards[s6] < 2:
                continue
            if p and s6 <= max(p) and len(p) == 12:
                continue
            if not p or len(p) == 12:
                m.removestraight(s1, s6)
                m.removestraight(s1, s6)
                if dfs(m, c, not myturn, [s1,s1,s2,s2,s3,s3,s4,s4,s5,s5,s6,s6]):
                    m.addstraight(s1, s6)
                    m.addstraight(s1, s6)
                    dp[key] = True
                    res[0] = [s1,s1,s2,s2,s3,s3,s4,s4,s5,s5,s6,s6]
                    return True
                m.addstraight(s1, s6)
                m.addstraight(s1, s6)
            s7 = s6 + 1
            if m.cards[s7] < 2:
                continue
            if p and s7 <= max(p) and len(p) == 14:
                continue
            if not p or len(p) == 14:
                m.removestraight(s1, s7)
                m.removestraight(s1, s7)
                if dfs(m, c, not myturn, [s1,s1,s2,s2,s3,s3,s4,s4,s5,s5,s6,s6,s7,s7]):
                    m.addstraight(s1, s7)
                    m.addstraight(s1, s7)
                    dp[key] = True
                    res[0] = [s1,s1,s2,s2,s3,s3,s4,s4,s5,s5,s6,s6,s7,s7]
                    return True
                m.addstraight(s1, s7)
                m.addstraight(s1, s7)
            s8 = s7 + 1
            if m.cards[s8] < 2:
                continue
            if p and s8 <= max(p) and len(p) == 16:
                continue
            if not p or len(p) == 16:
                m.removestraight(s1, s8)
                m.removestraight(s1, s8)
                if dfs(m, c, not myturn, [s1,s1,s2,s2,s3,s3,s4,s4,s5,s5,s6,s6,s7,s7,s8,s8]):
                    m.addstraight(s1, s8)
                    m.addstraight(s1, s8)
                    dp[key] = True
                    res[0] = [s1,s1,s2,s2,s3,s3,s4,s4,s5,s5,s6,s6,s7,s7,s8,s8]
                    return True
                m.addstraight(s1, s8)
                m.addstraight(s1, s8)
            s9 = s8 + 1
            if m.cards[s9] < 2:
                continue
            if p and s9 <= max(p) and len(p) == 18:
                continue
            if not p or len(p) == 18:
                m.removestraight(s1, s9)
                m.removestraight(s1, s9)
                if dfs(m, c, not myturn, [s1,s1,s2,s2,s3,s3,s4,s4,s5,s5,s6,s6,s7,s7,s8,s8,s9,s9]):
                    m.addstraight(s1, s9)
                    m.addstraight(s1, s9)
                    dp[key] = True
                    res[0] = [s1,s1,s2,s2,s3,s3,s4,s4,s5,s5,s6,s6,s7,s7,s8,s8,s9,s9]
                    return True
                m.addstraight(s1, s9)
                m.addstraight(s1, s9)
            s10 = s9 + 1
            if m.cards[s10] < 2:
                continue
            if p and s10 <= max(p) and len(p) == 20:
                continue
            if not p or len(p) == 20:
                m.removestraight(s1, s10)
                m.removestraight(s1, s10)
                if dfs(m, c, not myturn, [s1,s1,s2,s2,s3,s3,s4,s4,s5,s5,s6,s6,s7,s7,s8,s8,s9,s9,s10,s10]):
                    m.addstraight(s1, s10)
                    m.addstraight(s1, s10)
                    dp[key] = True
                    res[0] = [s1,s1,s2,s2,s3,s3,s4,s4,s5,s5,s6,s6,s7,s7,s8,s8,s9,s9,s10,s10]
                    return True
                m.addstraight(s1, s10)
                m.addstraight(s1, s10)


    if myturn:
        dp[key] = False
        return False
    else:
        dp[key] = True
        return True

def modify(arr):
    for i in range(len(arr)):
        if arr[i] == 2:
            arr[i] = 16
        if arr[i] == 1:
            arr[i] == 14
def solution():
    ans = []
    for poke in res[0]:
        if poke == 18:
            ans.append("JOKER")
        elif poke == 17:
            ans.append("JOKER2")
        elif poke == 16:
            ans.append(2)
        elif poke == 14:
            ans.append("ACE")
        elif poke == 13:
            ans.append("KING")
        elif poke == 12:
            ans.append("QUEEN")
        elif poke == 11:
            ans.append("JACK")
        else:
            ans.append(poke)
    print(ans)

JOKER = 18
JOKER2 = 17
KING = 13
K = 13
QUEEN = 12
Q = 12
JACK = 11
J = 11
A = 14
ACE = 14
import time
start_time = time.time()
comp = [JOKER2,2,A,K,J,J,10,10,9,7,7,5]
mine = [2,A,K,Q,Q,J,J,10,10,9,8,3]
comp = Pokers(comp)
mine = Pokers(mine)
if dfs(mine, comp, True):
    solution()
print("--- %s seconds ---" % (time.time() - start_time))
