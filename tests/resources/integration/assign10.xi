use io
use conv
main(args: int[][]) {
a: int[5][4]
{
i: int = 0
while (i < length(a)) {
j: int = 0
while (j < length(a[i])) {
a[i][j] = i + j
j = j + 1
}
i = i + 1
}
}
a[f(a, 4, 2)-3][f(a, 4, 2)-6] = 47
{
i: int = 0
while (i < length(a)) {
j: int = 0
while (j < length(a[i])) {
println(unparseInt(a[i][j]))
j = j + 1
}
i = i + 1
}
}
}
f(a: int[][], i: int, j: int): int {
print("Index: ")
print(unparseInt(i))
print(" ")
println(unparseInt(j))
a[i][j] = a[i][j] + 1
return a[i][j]
}