#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// ---------------------------
//  Base64 decode (simple)
// ---------------------------
static const unsigned char dtable[256] = {
    [ 'A' ]=0, [ 'B' ]=1, [ 'C' ]=2, [ 'D' ]=3, [ 'E' ]=4, [ 'F' ]=5,
    [ 'G' ]=6, [ 'H' ]=7, [ 'I' ]=8, [ 'J' ]=9, [ 'K' ]=10, [ 'L' ]=11,
    [ 'M' ]=12, [ 'N' ]=13, [ 'O' ]=14, [ 'P' ]=15, [ 'Q' ]=16, [ 'R' ]=17,
    [ 'S' ]=18, [ 'T' ]=19, [ 'U' ]=20, [ 'V' ]=21, [ 'W' ]=22, [ 'X' ]=23,
    [ 'Y' ]=24, [ 'Z' ]=25,
    [ 'a' ]=26, [ 'b' ]=27, [ 'c' ]=28, [ 'd' ]=29, [ 'e' ]=30, [ 'f' ]=31,
    [ 'g' ]=32, [ 'h' ]=33, [ 'i' ]=34, [ 'j' ]=35, [ 'k' ]=36, [ 'l' ]=37,
    [ 'm' ]=38, [ 'n' ]=39, [ 'o' ]=40, [ 'p' ]=41, [ 'q' ]=42, [ 'r' ]=43,
    [ 's' ]=44, [ 't' ]=45, [ 'u' ]=46, [ 'v' ]=47, [ 'w' ]=48, [ 'x' ]=49,
    [ 'y' ]=50, [ 'z' ]=51,
    [ '0' ]=52, [ '1' ]=53, [ '2' ]=54, [ '3' ]=55, [ '4' ]=56, [ '5' ]=57,
    [ '6' ]=58, [ '7' ]=59, [ '8' ]=60, [ '9' ]=61, [ '+' ]=62, [ '/' ]=63
};

unsigned char* base64_decode(const char* input, int* out_len) {
    int len = strlen(input);
    int pad = 0;

    if (input[len - 1] == '=') pad++;
    if (input[len - 2] == '=') pad++;

    int decoded_len = (len * 3) / 4 - pad;
    unsigned char* out = malloc(decoded_len);
    if (!out) return NULL;

    int j = 0;
    unsigned int val = 0;
    int valb = -8;

    for (int i = 0; i < len; i++) {
        unsigned char c = input[i];
        if (c == '=')
            break;

        unsigned char d = dtable[c];
        val = (val << 6) | d;
        valb += 6;

        if (valb >= 0) {
            out[j++] = (unsigned char)((val >> valb) & 0xFF);
            valb -= 8;
        }
    }

    *out_len = decoded_len;
    return out;
}

// ---------------------------
//    XOR decrypt function
// ---------------------------
char* d0x116_(const char* encoded) {
    const char* key = "A0x43x32x49$cwBJAQ==";
    int key_len = strlen(key);

    int data_len = 0;
    unsigned char* decoded = base64_decode(encoded, &data_len);
    if (!decoded) return NULL;

    char* result = malloc(data_len + 1);
    if (!result) {
        free(decoded);
        return NULL;
    }

    for (int i = 0; i < data_len; i++) {
        result[i] = decoded[i] ^ key[i % key_len];
    }

    result[data_len] = '\0';
    free(decoded);

    return result;
}

int main(int argc, char *argv[]) {
    if (argc < 2) {
        return 1;    
    }

    char *decoded = d0x116_(argv[1]);
    if (!decoded) {
        return 2;
    }

    printf("%s", decoded);   // IMPORTANT : pas de \n
    free(decoded);

    return 0;
}