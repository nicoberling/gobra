
package pkg;

func test() {
    //:: ExpectedOutput(assert_error:assertion_error)
	assert 0 == 1;
};

func evens(n int) (e int) {

  //:: ExpectedOutput(invariant_establishment_error:assertion_error)
  invariant e == n/2;
  for i := 1; i <= n; i++ {
    if(n % 2 == 0) { e++; };
  };
};

type Point struct {
  x int;
  y int;
};


//:: ExpectedOutput(contract_not_well_formed:permission_error)
ensures r.x == 0;
func Origin() (r *Point) {
  r = &Point{0, 0};
};