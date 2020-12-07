
// Creates a substring from indices start (inclusive) to end (exclusive)
substring(str: int[], start: int, end: int): int[] {
    sub: int[end - start];
    for (i: int = start; i < end; i = i + 1) {
        sub[i - start] = str[i];
    }
    return sub;
}

// Creates a copy of the string
copyString(str: int[]): int[] {
    return substring(str, 0, length(str));
}

// Trims whitespace on the left side of the string
trimStringLeft(str: int[]): int[] {
    count: int = 0;
    while count < length(str) {
        if str[count] != ' ' & str[count] != '\n' {
            return substring(str, count, length(str));
        }
    }
    return "";
}

// Trims whitespace on the right side of the string
trimStringRight(str: int[]): int[] {
    count: int = length(str) - 1;
    while count >= 0 {
        if str[count] != ' ' & str[count] != '\n' {
            return substring(str, 0, count + 1);
        }
    }
    return "";
}

// Trimes whitespace on the left and right sides of the string
trimString(str: int[]): int[] {
    leftTrim: int[] = trimStringLeft(str);
    rightTrim: int[] = trimStringRight(leftTrim);
    free(leftTrim);
    return rightTrim;
}

// Finds the index of [str] where [sub] begins. Returns -1 if [sub] is not in [str]
indexOfString(str: int[], sub: int[]): int {
    i: int = 0;
    highest: int = length(str) - length(sub)
    while i < highest {
        good: bool = true
        for (j: int = 0; i < length(sub) & good; j = j + 1) {
            if str[i + j] != sub[j] {
                good = false;
            }
        }
        if good {
            return i;
        }
    }
    return -1;
}

// Return a copy of the argument, with all lowercase letters translated to uppercase,
// using the US-ASCII character set.
uppercaseString(str: int[]): int[] {
    copy: int[] = copyString(str);
    offset: int = 'A' - 'a';
    for (i: int = 0; i < length(copy); i = i + 1) {
        if copy[i] >= 'a' & copy[i] <= 'z' {
            copy[i] = copy[i] + offset;
        }
    }
    return copy;
}

// Return a copy of the argument, with all lowercase letters translated to lowercase,
// using the US-ASCII character set.
lowercaseString(str: int[]): int[] {
    copy: int[] = copyString(str);
    offset: int = 'A' - 'a';
    for (i: int = 0; i < length(copy); i = i + 1) {
        if copy[i] >= 'A' & copy[i] <= 'Z' {
            copy[i] = copy[i] - offset;
        }
    }
    return copy;
}

// The comparison function for strings.
// Returns -1 if [str] < [str2]
// Returns 0 if [str1] == [str2]
// Returns 1 if [str1] > [str2]
// Comparison is done lexicographically.
compareString(str1: int[], str2: int[]): int {
    if (length(str1) < length(str2)) return -1;
    if (length(str2) > length(str2)) return 1;
    if (length(str1) == 0) return 0;
    i: int = 0;
    while (i < length(str1) && str1[i] == str2[i]) {
        i++;
    }
    return str1[i - 1] - str2[i - 1];
}
