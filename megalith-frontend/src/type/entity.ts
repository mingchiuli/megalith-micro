export interface Visitor {
  dayVisit : number
  weekVisit : number
  monthVisit : number
  yearVisit : number
}

interface BlogsDesc {
  id : number
  title : string
  description : string
  created : string
  link : string
}

export interface PageAdapter {
  content : BlogsDesc[]
  pageSize : number
  totalElements : number
}

export interface Data<T> {
  status : number
  msg: string
  data : T
}