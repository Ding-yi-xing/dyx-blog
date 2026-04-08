<template>
  <section class="grid gap-6 xl:grid-cols-[1.05fr_0.95fr]">
    <article class="rounded-[28px] bg-white p-6 shadow-sm">
      <h2 class="text-xl font-semibold text-slate-900">首页管理 / 更新节奏</h2>
      <p class="mt-2 text-sm leading-6 text-slate-500">
        首页第三屏会从已发布且被标记为“首页第三屏候选”的内容里自动聚合，这里提供维护入口。
      </p>

      <div class="mt-6 space-y-4 rounded-[24px] border border-slate-200 bg-slate-50 p-5">
        <div class="rounded-[20px] border border-slate-200 bg-white p-4">
          <p class="text-xs uppercase tracking-[0.22em] text-slate-400">当前策略</p>
          <h3 class="mt-2 text-lg font-semibold text-slate-900">自动聚合精选内容</h3>
          <p class="mt-3 text-sm leading-7 text-slate-600">
            第三屏会综合博客、动态、项目、作品与荣誉中的候选项，并按系统配置限制总条数与单类型条数。
          </p>
        </div>

        <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <RouterLink to="/dyx-manager/posts" class="rounded-[20px] border border-slate-200 bg-white p-4 transition hover:border-slate-300 hover:shadow-sm">
            <p class="text-sm font-semibold text-slate-900">去博客管理</p>
            <p class="mt-2 text-sm leading-6 text-slate-500">维护文章发布时间、摘要与首页第三屏候选状态。</p>
          </RouterLink>
          <RouterLink to="/dyx-manager/moments" class="rounded-[20px] border border-slate-200 bg-white p-4 transition hover:border-slate-300 hover:shadow-sm">
            <p class="text-sm font-semibold text-slate-900">去动态管理</p>
            <p class="mt-2 text-sm leading-6 text-slate-500">维护动态内容、时间与首页第三屏候选状态。</p>
          </RouterLink>
          <RouterLink to="/dyx-manager/resume" class="rounded-[20px] border border-slate-200 bg-white p-4 transition hover:border-slate-300 hover:shadow-sm">
            <p class="text-sm font-semibold text-slate-900">去简历管理</p>
            <p class="mt-2 text-sm leading-6 text-slate-500">维护项目经历，并决定哪些项目进入首页第三屏。</p>
          </RouterLink>
          <RouterLink to="/dyx-manager/works" class="rounded-[20px] border border-slate-200 bg-white p-4 transition hover:border-slate-300 hover:shadow-sm">
            <p class="text-sm font-semibold text-slate-900">去作品管理</p>
            <p class="mt-2 text-sm leading-6 text-slate-500">补充作品展示，并标记首页第三屏候选内容。</p>
          </RouterLink>
          <RouterLink to="/dyx-manager/honors" class="rounded-[20px] border border-slate-200 bg-white p-4 transition hover:border-slate-300 hover:shadow-sm">
            <p class="text-sm font-semibold text-slate-900">去荣誉管理</p>
            <p class="mt-2 text-sm leading-6 text-slate-500">维护获奖与成果内容，并标记首页第三屏候选内容。</p>
          </RouterLink>
          <RouterLink to="/dyx-manager/system-config" class="rounded-[20px] border border-slate-200 bg-white p-4 transition hover:border-slate-300 hover:shadow-sm">
            <p class="text-sm font-semibold text-slate-900">去系统配置</p>
            <p class="mt-2 text-sm leading-6 text-slate-500">调整参与类型、总条数、单类型条数和页脚文案。</p>
          </RouterLink>
        </div>
      </div>
    </article>

    <article class="rounded-[28px] bg-white p-6 shadow-sm">
      <h3 class="text-lg font-semibold text-slate-900">第三屏说明</h3>
      <div class="mt-5 space-y-4 text-sm leading-7 text-slate-600">
        <p>第三屏不维护独立内容表，避免首页和内容模块出现双份数据。</p>
        <p>前台卡片会根据内容类型跳转到对应页面：文章与动态进入详情页，项目跳转到简历页，作品与荣誉跳转到关于我页。</p>
      </div>

      <div class="mt-6 rounded-[24px] border border-dashed border-slate-300 bg-slate-50 p-5">
        <p class="text-xs uppercase tracking-[0.22em] text-slate-400">Preview</p>
        <div class="mt-4 space-y-3">
          <div class="rounded-[18px] bg-white p-4 shadow-sm">
            <p class="text-sm font-semibold text-slate-900">第三屏精选内容</p>
            <p class="mt-2 text-sm text-slate-500">前台会聚合候选内容，并在移动端保持单屏翻页、屏内滚动的浏览方式。</p>
          </div>
          <div class="grid gap-3">
            <div class="rounded-[18px] border border-slate-200 bg-white p-4 text-sm text-slate-600">每个内容模块都可以单独决定是否参与第三屏聚合。</div>
            <div class="rounded-[18px] border border-slate-200 bg-white p-4 text-sm text-slate-600">系统配置可控制总条数、单类型上限和第三屏页脚文案。</div>
          </div>
        </div>
      </div>
    </article>
  </section>
</template>
