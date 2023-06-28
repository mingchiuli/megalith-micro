export interface Visitor {
  dayVisit: number
  weekVisit: number
  monthVisit: number
  yearVisit: number
}

export interface Data<T> {
  status : number
  msg: string
  data : T
}