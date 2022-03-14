package predconstr


pred larger(i int, j int) {
	i > j
}

ensures larger{11, _}(res)
func foo() (res int) {
	return 10
}