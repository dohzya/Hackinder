/** @jsx React.DOM */

var App = React.createClass({
  getInitialState: function(){
    return {
      me : null,
      notifications : []
    };
  },
  loadState : function(data){
    console.log(arguments);
    this.setState(data);
  },
  componentDidMount : function(){
    $.getJSON("/me").then(this.loadState);
  },
  render : function(){
    return <div><Notification/><YourProject /><Projects /><Hackers /></div>;
  }
});

var Notification = React.createClass({
  getInitialState:function(){
    return {
      notifications:[]
    };
  },
  componentDidMount : function(){
    $.getJSON("/notifications").then(this.loadState);
  },
  loadState:function( state ){
    this.setState({
      notifications:state
    });
  },
  render : function(){
    var form = this.state.notifications.map(function(n){
      if(n.type === "participation") return <Participation key={n.id} remove={this.remove.bind(this, n.id)}/>;
      else "";
    }, this);
    return <div className="notification">{form}</div>;
  },
  remove : function(id){
    var newNotif = _.filter(this.state.notifications, function( n ){
        return id != n.id;
     }, this)

    this.setState({
      notifications: newNotif
    });
  }
});

var Participation = React.createClass({
  getInitialState : function(){
    return {
      step : 0
    };
  },
  render:function(){
    var steps = [
      this.viewStep1,
      this.viewStep2
    ];
    return steps[this.state.step]();
  },
  accept: function(e){
    this.setState({
      step: 1
    });
    e.preventDefault();
    setTimeout(this.props.remove, 1000)
  },
  refuse : function(){

  },
  viewStep1 : function(){
    return <div className="wrapper participation-1">
        <h1>Veux-tu participer au prochain Hackday ?</h1>
        <p className="details">Une petite réponse avant mercredi 23 avril et on serait ravi !</p>
        <div className="buttons">
                <a href="" className="button polygon" onClick={this.accept}>oh que oui !</a>
                <a href="" onClick={this.refuse}>non</a>
        </div>
    </div>;
  },
  viewStep2 : function(){
    return <div className="wrapper participation-3">
        <h1>Très heureux de te compter parmi nous :-)</h1>
    </div>
  }
});

var YourProject = React.createClass({
  render : function(){
    return <div><formProject/></div>;
  }
});

var Projects = React.createClass({
  render : function(){
    return <div></div>;
  }
});

var Hackers = React.createClass({
  render : function(){
    return <div></div>;
  }
});

var createButton = React.createClass({
  onClick: function () {
    console.log('CLICK', router.currentState().params);
    go({create: true});
  },
  render: function () {
    console.log(this.props);
    return <div className="new-project">
      <button type="button" onClick={this.onClick}>Crée ton projet</button>
    </div>
  }
});

var formProject = React.createClass({
  onSubmit: function () {
    console.log('onSubmit', this.props, this.state);
    go({create: false});
  },
  handleName: function (event) {
    this.setState({name: event.target.value});
  },
  handleDescription: function (event) {
    this.setState({description: event.target.value});
  },
  handleQuote: function (event) {
    this.setState({quote: event.target.value});
  },
  render: function() {
    if (this.props.data) {
      this.setState(this.props.data);
    }

    return <div className="new-project-2">
      <h1>Crée ton projet</h1>
      <form onSubmit={this.onSubmit}>
        <input type="text" onChange={this.handleName} placeholder="Nom du projet"/>
        <textarea onChange={this.handleDescription} placeholder="Description"/>
        <input type="text" onChange={this.handleQuote} placeholder="Il faut venir dans mon équipe paske..."/>
        <button type="submit">GO !</button>
      </form>
    </div>;
  }
});

var toggleCreate = function (create) {
  if(create){
    React.renderComponent(<formProject/>, document.getElementById('new-project'));
  }
  else {
    React.renderComponent(<createButton/>, document.getElementById('new-project'));
  }
}


var createApp = function(){
  return React.renderComponent(<App/>, document.getElementById("app"));
};
