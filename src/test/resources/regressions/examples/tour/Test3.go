package trivial;

requires acc(*p);
ensures acc(*p) && *p == v;
func set(p *int, v int) {
  *p = v;
};

func pointer1() {
  i! := 5;
  p := &i;
  *p = 7;
  assert i == 7;
};

func pointer2() {
  i! := 5;
  set(&i, 7);
  assert i == 7;
};

requires acc(*p) && acc(*q);
ensures acc(*p) && acc(*q);
ensures *p == old(*q);
ensures *q == old(*p);
func swap(p, q *int) {
  tmp := *p;
  *p = *q;
  *q = tmp;
};

func pointer3() {
  i! := 5;
  j! := 7;
  swap(&i, &j);
  assert i == 7;
};

type Point struct {
  x int;
  y int;
};


func OriginV() (r Point) {
  r = Point{0, 0};
};


func mirror() {
  p := Point{5, 7};
  assert p.x == 5;
  tmp := p.x;
  p.x = p.y;
  p.y = tmp;
  assert p.x == 7;
};

ensures acc(r.x) && r.x == 0;
ensures acc(r.y) && r.y == 0;
func Origin() (r *Point) {
  r = &Point{0, 0};
};

/* not supported yet
func init() {
  a := Point{y: 5};
  assert a.x == 0 && a.y == 5;

  b := Point{};
  assert b.x == 0 && b.y == 0;
};*/

