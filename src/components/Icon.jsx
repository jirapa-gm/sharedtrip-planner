// ไอคอนเส้นกลมมนแบบเดียวกันทั้งแอป
const paths = {
  plane: <path d="M10.5 13.5 4 11l1.5-1.5 7 1L17 6a2 2 0 0 1 3 3l-4.5 4.5 1 7L15 22l-2.5-6.5L9 19v2.5L7.5 23 6 19l-4-1.5L3.5 16H6l3.5-3.5" />,
  cloud: <path d="M7 18h10a4 4 0 0 0 .5-7.97A6 6 0 0 0 6.1 9.5 4.25 4.25 0 0 0 7 18Z" />,
  alert: <><path d="M12 3 2.5 20h19L12 3Z" /><path d="M12 10v4" /><path d="M12 17.2v.1" /></>,
  undo: <><path d="M9 14 4 9l5-5" /><path d="M4 9h10.5a5.5 5.5 0 0 1 0 11H11" /></>,
  redo: <><path d="m15 14 5-5-5-5" /><path d="M20 9H9.5a5.5 5.5 0 0 0 0 11H13" /></>,
  save: <><path d="M12 4v11" /><path d="m7 10 5 5 5-5" /><path d="M5 20h14" /></>,
  folder: <path d="M3 7a2 2 0 0 1 2-2h4l2 2.5h8a2 2 0 0 1 2 2V17a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V7Z" />,
  plus: <><path d="M12 5v14" /><path d="M5 12h14" /></>,
  close: <><path d="m6 6 12 12" /><path d="M18 6 6 18" /></>,
}

export default function Icon({ name, size = 18 }) {
  return (
    <svg className="icon" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor"
      strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      {paths[name]}
    </svg>
  )
}
