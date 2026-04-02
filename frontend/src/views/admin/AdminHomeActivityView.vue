<template>
  <!--
    后台首页更新节奏说明页。
    当前用于说明第三屏采用自动聚合策略，并提供跳转到各内容管理页的快捷入口。
  -->
  <section class="grid gap-6 xl:grid-cols-[1.05fr_0.95fr]">
    <article class="rounded-[28px] bg-white p-6 shadow-sm">
      <h2 class="text-xl font-semibold text-slate-900">首页管理 / 更新节奏</h2>
      <p class="mt-2 text-sm leading-6 text-slate-500">
        首页第三屏当前仍自动聚合博客、动态、作品等最新内容，这里先提供预览和维护入口。
      </p>

      <div class="mt-6 space-y-4 rounded-[24px] border border-slate-200 bg-slate-50 p-5">
        <div class="rounded-[20px] border border-slate-200 bg-white p-4">
          <p class="text-xs uppercase tracking-[0.22em] text-slate-400">当前策略</p>
          <h3 class="mt-2 text-lg font-semibold text-slate-900">自动聚合最近更新</h3>
          <p class="mt-3 text-sm leading-7 text-slate-600">
            第三屏会根据已发布的博客、动态、作品与荣誉内容自动整理更新时间轴，不需要单独维护一套首页专用数据。
          </p>
        </div>

        <div class="grid gap-4 sm:grid-cols-2">
          <RouterLink to="/dyx-manager/posts" class="rounded-[20px] border border-slate-200 bg-white p-4 transition hover:border-slate-300 hover:shadow-sm">
            <p class="text-sm font-semibold text-slate-900">去博客管理</p>
            <p class="mt-2 text-sm leading-6 text-slate-500">调整文章发布时间、封面与摘要。</p>
          </RouterLink>
          <RouterLink to="/dyx-manager/moments" class="rounded-[20px] border border-slate-200 bg-white p-4 transition hover:border-slate-300 hover:shadow-sm">
            <p class="text-sm font-semibold text-slate-900">去动态管理</p>
            <p class="mt-2 text-sm leading-6 text-slate-500">维护近期动态内容和展示顺序。</p>
          </RouterLink>
          <RouterLink to="/dyx-manager/works" class="rounded-[20px] border border-slate-200 bg-white p-4 transition hover:border-slate-300 hover:shadow-sm">
            <p class="text-sm font-semibold text-slate-900">去作品管理</p>
            <p class="mt-2 text-sm leading-6 text-slate-500">补充作品展示，影响首页更新节奏卡片。</p>
          </RouterLink>
          <RouterLink to="/dyx-manager/honors" class="rounded-[20px] border border-slate-200 bg-white p-4 transition hover:border-slate-300 hover:shadow-sm">
            <p class="text-sm font-semibold text-slate-900">去荣誉管理</p>
            <p class="mt-2 text-sm leading-6 text-slate-500">维护获奖与成果内容。</p>
          </RouterLink>
        </div>
      </div>
    </article>

    <article class="rounded-[28px] bg-white p-6 shadow-sm">
      <h3 class="text-lg font-semibold text-slate-900">第三屏说明</h3>
      <div class="mt-5 space-y-4 text-sm leading-7 text-slate-600">
        <p>这里先不新增独立配置模型，避免首页第三屏和内容管理数据重复维护。</p>
        <p>如果后续你希望把第三屏标题、说明文案或排序逻辑也单独抽到后台配置，再在这一页继续扩展即可。</p>
      </div>

      <div class="mt-6 rounded-[24px] border border-dashed border-slate-300 bg-slate-50 p-5">
        <p class="text-xs uppercase tracking-[0.22em] text-slate-400">Preview</p>
        <div class="mt-4 space-y-3">
          <div class="rounded-[18px] bg-white p-4 shadow-sm">
            <p class="text-sm font-semibold text-slate-900">最近更新节奏</p>
            <p class="mt-2 text-sm text-slate-500">前台会自动汇总最近发布和最近更新的内容。</p>
          </div>
          <div class="grid gap-3">
            <div class="rounded-[18px] border border-slate-200 bg-white p-4 text-sm text-slate-600">博客、动态、作品、荣誉将按更新时间形成节奏卡片。</div>
            <div class="rounded-[18px] border border-slate-200 bg-white p-4 text-sm text-slate-600">无需额外录入首页专用条目，维护成本更低。</div>
          </div>
        </div>
      </div>
    </article>
  </section>
</template>
