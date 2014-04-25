/** @jsx React.DOM */

document.addEventListener('submit', function (event) {
  event.preventDefault();
});

// $(".main").onepage_scroll({
//   sectionContainer: "section",
//   easing: "ease",
//   animationTime: 1000,
//   pagination: true,
//   updateURL: false
// });

var Router = Abyssa.Router,
    State = Abyssa.State,
    go = function (params) {
      router.state('home', _.defaults(params, router.currentState().params)).done();
    };

var router = Router({
  home: State('/?create&edit&slide', {
    data: {
      create: false,
      edit: false,
      slide: 0
    },
    enter: function (params) {
      this.data('create', params.create);
      this.data('edit', params.edit);
      this.data('slide', params.slide);

      createApp();
      //toggleCreate(params.create);
      // toggleEdit(params.edit);
      // slideTo(params.slide);
    },
    update: function (params) {
      console.log('Update page');

      // CREATE PROJECT
      if (params.create !== undefined && params.create !== this.data('create')) {
        this.data('create', params.create);
        toggleCreate(params.create);
      }

      // EDIT PROJECT
      if (params.edit !== undefined && params.edit !== this.data('edit')) {
        this.data('edit', params.edit);
        toggleEdit(params.edit);
      }

      // PROJECTS SLIDER
      if (params.slide !== undefined && params.slide !== this.data('slide')) {
        this.data('slide', params.slide);
        slideTo(params.slide);
      }
    }
  })
})
.configure({
  enableLogs: true,
  notFound: '/'
})
.init();
