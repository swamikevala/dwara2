package org.ishafoundation.dwaraapi.api.resp.dwarahover;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public class StockWords {
    final List<String> stopWords = Arrays.asList(
            "ll",
            "tis",
            "twas",
            "ve",
            "10",
            "39",
            "a",
            "as",
            "able",
            "ableabout",
            "about",
            "above",
            "abroad",
            "abst",
            "accordance",
            "according",
            "accordingly",
            "across",
            "act",
            "actually",
            "ad",
            "added",
            "adj",
            "adopted",
            "ae",
            "af",
            "affected",
            "affecting",
            "affects",
            "after",
            "afterwards",
            "ag",
            "again",
            "against",
            "ago",
            "ah",
            "ahead",
            "ai",
            "aint",
            "aint",
            "al",
            "all",
            "allow",
            "allows",
            "almost",
            "alone",
            "along",
            "alongside",
            "already",
            "also",
            "although",
            "always",
            "am",
            "amid",
            "amidst",
            "among",
            "amongst",
            "amoungst",
            "amount",
            "an",
            "and",
            "announce",
            "another",
            "any",
            "anybody",
            "anyhow",
            "anymore",
            "anyone",
            "anything",
            "anyway",
            "anyways",
            "anywhere",
            "ao",
            "apart",
            "apparently",
            "appear",
            "appreciate",
            "appropriate",
            "approximately",
            "aq",
            "ar",
            "are",
            "area",
            "areas",
            "aren",
            "arent",
            "arent",
            "arise",
            "around",
            "arpa",
            "as",
            "aside",
            "ask",
            "asked",
            "asking",
            "asks",
            "associated",
            "at",
            "au",
            "auth",
            "available",
            "aw",
            "away",
            "awfully",
            "az",
            "b",
            "ba",
            "back",
            "backed",
            "backing",
            "backs",
            "backward",
            "backwards",
            "bb",
            "bd",
            "be",
            "became",
            "because",
            "become",
            "becomes",
            "becoming",
            "been",
            "before",
            "beforehand",
            "began",
            "begin",
            "beginning",
            "beginnings",
            "begins",
            "behind",
            "being",
            "beings",
            "believe",
            "below",
            "beside",
            "besides",
            "best",
            "better",
            "between",
            "beyond",
            "bf",
            "bg",
            "bh",
            "bi",
            "big",
            "bill",
            "billion",
            "biol",
            "bj",
            "bm",
            "bn",
            "bo",
            "both",
            "bottom",
            "br",
            "brief",
            "briefly",
            "bs",
            "bt",
            "but",
            "buy",
            "bv",
            "bw",
            "by",
            "bz",
            "c",
            "cmon",
            "cs",
            "ca",
            "call",
            "came",
            "can",
            "cant",
            "cannot",
            "cant",
            "caption",
            "case",
            "cases",
            "cause",
            "causes",
            "cc",
            "cd",
            "certain",
            "certainly",
            "cf",
            "cg",
            "ch",
            "changes",
            "ci",
            "ck",
            "cl",
            "clear",
            "clearly",
            "click",
            "cm",
            "cmon",
            "cn",
            "co",
            "co.",
            "com",
            "come",
            "comes",
            "computer",
            "con",
            "concerning",
            "consequently",
            "consider",
            "considering",
            "contain",
            "containing",
            "contains",
            "copy",
            "corresponding",
            "could",
            "couldve",
            "couldn",
            "couldnt",
            "couldnt",
            "course",
            "cr",
            "cry",
            "cs",
            "cu",
            "currently",
            "cv",
            "cx",
            "cy",
            "cz",
            "d",
            "dare",
            "darent",
            "darent",
            "date",
            "de",
            "dear",
            "definitely",
            "describe",
            "described",
            "despite",
            "detail",
            "did",
            "didn",
            "didnt",
            "didnt",
            "differ",
            "different",
            "differently",
            "directly",
            "dj",
            "dk",
            "dm",
            "do",
            "does",
            "doesn",
            "doesnt",
            "doesnt",
            "doing",
            "don",
            "dont",
            "done",
            "dont",
            "doubtful",
            "down",
            "downed",
            "downing",
            "downs",
            "downwards",
            "due",
            "during",
            "dz",
            "e",
            "each",
            "early",
            "ec",
            "ed",
            "edu",
            "ee",
            "effect",
            "eg",
            "eh",
            "eight",
            "eighty",
            "either",
            "eleven",
            "else",
            "elsewhere",
            "empty",
            "end",
            "ended",
            "ending",
            "ends",
            "enough",
            "entirely",
            "er",
            "es",
            "especially",
            "et",
            "et-al",
            "etc",
            "even",
            "evenly",
            "ever",
            "evermore",
            "every",
            "everybody",
            "everyone",
            "everything",
            "everywhere",
            "ex",
            "exactly",
            "example",
            "except",
            "f",
            "face",
            "faces",
            "fact",
            "facts",
            "fairly",
            "far",
            "farther",
            "felt",
            "few",
            "fewer",
            "ff",
            "fi",
            "fifteen",
            "fifth",
            "fifty",
            "fify",
            "fill",
            "find",
            "finds",
            "fire",
            "first",
            "five",
            "fix",
            "fj",
            "fk",
            "fm",
            "fo",
            "followed",
            "following",
            "follows",
            "for",
            "forever",
            "former",
            "formerly",
            "forth",
            "forty",
            "forward",
            "found",
            "four",
            "fr",
            "free",
            "from",
            "From",
            "front",
            "full",
            "fully",
            "further",
            "furthered",
            "furthering",
            "furthermore",
            "furthers",
            "fx",
            "g",
            "ga",
            "gave",
            "gb",
            "gd",
            "ge",
            "general",
            "generally",
            "get",
            "gets",
            "getting",
            "gf",
            "gg",
            "gh",
            "gi",
            "give",
            "given",
            "gives",
            "giving",
            "gl",
            "gm",
            "gmt",
            "gn",
            "go",
            "goes",
            "going",
            "gone",
            "good",
            "goods",
            "got",
            "gotten",
            "gov",
            "gp",
            "gq",
            "gr",
            "great",
            "greater",
            "greatest",
            "greetings",
            "group",
            "grouped",
            "grouping",
            "groups",
            "gs",
            "gt",
            "gu",
            "gw",
            "gy",
            "h",
            "had",
            "hadnt",
            "hadnt",
            "half",
            "happens",
            "hardly",
            "has",
            "hasn",
            "hasnt",
            "hasnt",
            "have",
            "haven",
            "havent",
            "havent",
            "having",
            "he",
            "hed",
            "hell",
            "hes",
            "hed",
            "hell",
            "hello",
            "help",
            "hence",
            "her",
            "here",
            "heres",
            "hereafter",
            "hereby",
            "herein",
            "heres",
            "hereupon",
            "hers",
            "herself",
            "herse”",
            "hes",
            "hi",
            "hid",
            "high",
            "higher",
            "highest",
            "him",
            "himself",
            "himse”",
            "his",
            "hither",
            "hk",
            "hm",
            "hn",
            "home",
            "homepage",
            "hopefully",
            "how",
            "howd",
            "howll",
            "hows",
            "howbeit",
            "however",
            "hr",
            "ht",
            "htm",
            "html",
            "http",
            "hu",
            "hundred",
            "i",
            "id",
            "ill",
            "im",
            "ive",
            "i.e.",
            "id",
            "ie",
            "if",
            "ignored",
            "ii",
            "il",
            "ill",
            "im",
            "immediate",
            "immediately",
            "importance",
            "important",
            "in",
            "inasmuch",
            "inc",
            "inc.",
            "indeed",
            "index",
            "indicate",
            "indicated",
            "indicates",
            "information",
            "inner",
            "inside",
            "insofar",
            "instead",
            "int",
            "interest",
            "interested",
            "interesting",
            "interests",
            "into",
            "invention",
            "inward",
            "io",
            "iq",
            "ir",
            "is",
            "isn",
            "isnt",
            "isnt",
            "it",
            "itd",
            "itll",
            "its",
            "itd",
            "itll",
            "its",
            "itself",
            "itse”",
            "ive",
            "j",
            "je",
            "jm",
            "jo",
            "join",
            "jp",
            "just",
            "k",
            "ke",
            "keep",
            "keeps",
            "kept",
            "keys",
            "kg",
            "kh",
            "ki",
            "kind",
            "km",
            "kn",
            "knew",
            "know",
            "known",
            "knows",
            "kp",
            "kr",
            "kw",
            "ky",
            "kz",
            "l",
            "la",
            "large",
            "largely",
            "last",
            "lately",
            "later",
            "latest",
            "latter",
            "latterly",
            "lb",
            "lc",
            "least",
            "length",
            "less",
            "lest",
            "let",
            "lets",
            "lets",
            "li",
            "like",
            "liked",
            "likely",
            "likewise",
            "line",
            "little",
            "lk",
            "ll",
            "long",
            "longer",
            "longest",
            "look",
            "looking",
            "looks",
            "low",
            "lower",
            "lr",
            "ls",
            "lt",
            "ltd",
            "lu",
            "lv",
            "ly",
            "m",
            "ma",
            "made",
            "mainly",
            "make",
            "makes",
            "making",
            "man",
            "many",
            "may",
            "maybe",
            "maynt",
            "maynt",
            "mc",
            "md",
            "me",
            "mean",
            "means",
            "meantime",
            "meanwhile",
            "member",
            "members",
            "men",
            "merely",
            "mg",
            "mh",
            "microsoft",
            "might",
            "mightve",
            "mightnt",
            "mightnt",
            "mil",
            "mill",
            "million",
            "mine",
            "minus",
            "miss",
            "mk",
            "ml",
            "mm",
            "mn",
            "mo",
            "more",
            "moreover",
            "most",
            "mostly",
            "move",
            "mp",
            "mq",
            "mr",
            "mrs",
            "ms",
            "msie",
            "mt",
            "mu",
            "much",
            "mug",
            "must",
            "mustve",
            "mustnt",
            "mustnt",
            "mv",
            "mw",
            "mx",
            "my",
            "myself",
            "myse”",
            "mz",
            "n",
            "na",
            "name",
            "namely",
            "nay",
            "nc",
            "nd",
            "ne",
            "near",
            "nearly",
            "necessarily",
            "necessary",
            "need",
            "needed",
            "needing",
            "neednt",
            "neednt",
            "needs",
            "neither",
            "net",
            "netscape",
            "never",
            "neverf",
            "neverless",
            "nevertheless",
            "new",
            "newer",
            "newest",
            "next",
            "nf",
            "ng",
            "ni",
            "nine",
            "ninety",
            "nl",
            "no",
            "no-one",
            "nobody",
            "non",
            "none",
            "nonetheless",
            "noone",
            "nor",
            "normally",
            "nos",
            "not",
            "noted",
            "nothing",
            "notwithstanding",
            "novel",
            "now",
            "nowhere",
            "np",
            "nr",
            "nu",
            "null",
            "number",
            "numbers",
            "nz",
            "o",
            "obtain",
            "obtained",
            "obviously",
            "of",
            "off",
            "often",
            "oh",
            "ok",
            "okay",
            "old",
            "older",
            "oldest",
            "om",
            "omitted",
            "on",
            "once",
            "one",
            "ones",
            "ones",
            "only",
            "onto",
            "open",
            "opened",
            "opening",
            "opens",
            "opposite",
            "or",
            "ord",
            "order",
            "ordered",
            "ordering",
            "orders",
            "org",
            "other",
            "others",
            "otherwise",
            "ought",
            "oughtnt",
            "oughtnt",
            "our",
            "ours",
            "ourselves",
            "out",
            "outside",
            "over",
            "overall",
            "owing",
            "own",
            "p",
            "pa",
            "page",
            "pages",
            "part",
            "parted",
            "particular",
            "particularly",
            "parting",
            "parts",
            "past",
            "pe",
            "per",
            "perhaps",
            "pf",
            "pg",
            "ph",
            "pk",
            "pl",
            "place",
            "placed",
            "places",
            "please",
            "plus",
            "pm",
            "pmid",
            "pn",
            "point",
            "pointed",
            "pointing",
            "points",
            "poorly",
            "possible",
            "possibly",
            "potentially",
            "pp",
            "pr",
            "predominantly",
            "present",
            "presented",
            "presenting",
            "presents",
            "presumably",
            "previously",
            "primarily",
            "probably",
            "problem",
            "problems",
            "promptly",
            "proud",
            "provided",
            "provides",
            "pt",
            "put",
            "puts",
            "pw",
            "py",
            "q",
            "qa",
            "que",
            "quickly",
            "quite",
            "qv",
            "r",
            "ran",
            "rather",
            "rd",
            "re",
            "readily",
            "really",
            "reasonably",
            "recent",
            "recently",
            "ref",
            "refs",
            "regarding",
            "regardless",
            "regards",
            "related",
            "relatively",
            "research",
            "reserved",
            "respectively",
            "resulted",
            "resulting",
            "results",
            "right",
            "ring",
            "ro",
            "room",
            "rooms",
            "round",
            "ru",
            "run",
            "rw",
            "s",
            "sa",
            "said",
            "same",
            "saw",
            "say",
            "saying",
            "says",
            "sb",
            "sc",
            "sd",
            "se",
            "sec",
            "second",
            "secondly",
            "seconds",
            "section",
            "see",
            "seeing",
            "seem",
            "seemed",
            "seeming",
            "seems",
            "seen",
            "sees",
            "self",
            "selves",
            "sensible",
            "sent",
            "serious",
            "seriously",
            "seven",
            "seventy",
            "several",
            "sg",
            "sh",
            "shall",
            "shant",
            "shant",
            "she",
            "shed",
            "shell",
            "shes",
            "shed",
            "shell",
            "shes",
            "should",
            "shouldve",
            "shouldn",
            "shouldnt",
            "shouldnt",
            "show",
            "showed",
            "showing",
            "shown",
            "showns",
            "shows",
            "si",
            "side",
            "sides",
            "significant",
            "significantly",
            "similar",
            "similarly",
            "since",
            "sincere",
            "site",
            "six",
            "sixty",
            "sj",
            "sk",
            "sl",
            "slightly",
            "sm",
            "small",
            "smaller",
            "smallest",
            "sn",
            "so",
            "some",
            "somebody",
            "someday",
            "somehow",
            "someone",
            "somethan",
            "something",
            "sometime",
            "sometimes",
            "somewhat",
            "somewhere",
            "soon",
            "sorry",
            "specifically",
            "specified",
            "specify",
            "specifying",
            "sr",
            "st",
            "state",
            "states",
            "still",
            "stop",
            "strongly",
            "su",
            "sub",
            "substantially",
            "successfully",
            "such",
            "sufficiently",
            "suggest",
            "sup",
            "sure",
            "sv",
            "sy",
            "system",
            "sz",
            "t",
            "ts",
            "take",
            "taken",
            "taking",
            "tc",
            "td",
            "tell",
            "ten",
            "tends",
            "test",
            "text",
            "tf",
            "tg",
            "th",
            "than",
            "thank",
            "thanks",
            "thanx",
            "that",
            "thatll",
            "thats",
            "thatve",
            "thatll",
            "thats",
            "thatve",
            "the",
            "their",
            "theirs",
            "them",
            "themselves",
            "then",
            "thence",
            "there",
            "thered",
            "therell",
            "therere",
            "theres",
            "thereve",
            "thereafter",
            "thereby",
            "thered",
            "therefore",
            "therein",
            "therell",
            "thereof",
            "therere",
            "theres",
            "thereto",
            "thereupon",
            "thereve",
            "these",
            "they",
            "theyd",
            "theyll",
            "theyre",
            "theyve",
            "theyd",
            "theyll",
            "theyre",
            "theyve",
            "thick",
            "thin",
            "thing",
            "things",
            "think",
            "thinks",
            "third",
            "thirty",
            "this",
            "thorough",
            "thoroughly",
            "those",
            "thou",
            "though",
            "thoughh",
            "thought",
            "thoughts",
            "thousand",
            "three",
            "throug",
            "through",
            "throughout",
            "thru",
            "thus",
            "til",
            "till",
            "tip",
            "tis",
            "tj",
            "tk",
            "tm",
            "tn",
            "to",
            "today",
            "together",
            "too",
            "took",
            "top",
            "toward",
            "towards",
            "tp",
            "tr",
            "tried",
            "tries",
            "trillion",
            "truly",
            "try",
            "trying",
            "ts",
            "tt",
            "turn",
            "turned",
            "turning",
            "turns",
            "tv",
            "tw",
            "twas",
            "twelve",
            "twenty",
            "twice",
            "two",
            "tz",
            "u",
            "ua",
            "ug",
            "uk",
            "um",
            "un",
            "under",
            "underneath",
            "undoing",
            "unfortunately",
            "unless",
            "unlike",
            "unlikely",
            "until",
            "unto",
            "up",
            "upon",
            "ups",
            "upwards",
            "us",
            "use",
            "used",
            "useful",
            "usefully",
            "usefulness",
            "uses",
            "using",
            "usually",
            "uucp",
            "uy",
            "uz",
            "v",
            "va",
            "value",
            "various",
            "vc",
            "ve",
            "versus",
            "very",
            "vg",
            "vi",
            "via",
            "viz",
            "vn",
            "vol",
            "vols",
            "vs",
            "vu",
            "w",
            "want",
            "wanted",
            "wanting",
            "wants",
            "was",
            "wasn",
            "wasnt",
            "wasnt",
            "way",
            "ways",
            "we",
            "wed",
            "well",
            "were",
            "weve",
            "web",
            "webpage",
            "website",
            "wed",
            "welcome",
            "well",
            "wells",
            "went",
            "were",
            "weren",
            "werent",
            "werent",
            "weve",
            "wf",
            "what",
            "whatd",
            "whatll",
            "whats",
            "whatve",
            "whatever",
            "whatll",
            "whats",
            "whatve",
            "when",
            "whend",
            "whenll",
            "whens",
            "whence",
            "whenever",
            "where",
            "whered",
            "wherell",
            "wheres",
            "whereafter",
            "whereas",
            "whereby",
            "wherein",
            "wheres",
            "whereupon",
            "wherever",
            "whether",
            "which",
            "whichever",
            "while",
            "whilst",
            "whim",
            "whither",
            "who",
            "whod",
            "wholl",
            "whos",
            "whod",
            "whoever",
            "whole",
            "wholl",
            "whom",
            "whomever",
            "whos",
            "whose",
            "why",
            "whyd",
            "whyll",
            "whys",
            "widely",
            "width",
            "will",
            "willing",
            "wish",
            "with",
            "within",
            "without",
            "won",
            "wont",
            "wonder",
            "wont",
            "words",
            "work",
            "worked",
            "working",
            "works",
            "world",
            "would",
            "wouldve",
            "wouldn",
            "wouldnt",
            "wouldnt",
            "ws",
            "www",
            "x",
            "y",
            "ye",
            "year",
            "years",
            "yes",
            "yet",
            "you",
            "youd",
            "youll",
            "youre",
            "youve",
            "youd",
            "youll",
            "young",
            "younger",
            "youngest",
            "your",
            "youre",
            "yours",
            "yourself",
            "yourselves",
            "youve",
            "yt",
            "yu",
            "z",
            "za",
            "zero",
            "zm",
            "zr",
            "sadhguru",
            "sharing",
            "sharings",
            "iyc",
            "iii",
            "meeting",
            "interview",
            "online"
    );
}